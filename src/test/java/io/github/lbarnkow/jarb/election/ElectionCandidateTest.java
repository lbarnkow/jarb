/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb.election;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.RUNNING_FOR_ELECTION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.LINUX;

import io.github.lbarnkow.jarb.taskmanager.TaskEndedCallback;
import io.github.lbarnkow.jarb.taskmanager.TaskEndedEvent;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

class ElectionCandidateTest implements ElectionCandidateListener, TaskEndedCallback {

  private static final long DEFAULT_LEASE_TTL = 1000L;

  private final ElectionConfiguration config = new ElectionConfiguration();

  private Map<ElectionCandidate, ElectionCandidateState> states = new HashMap<>();
  private Semaphore waitForElection = new Semaphore(0);
  private Semaphore taskEndedSemaphore = new Semaphore(0);

  @Test
  void testIdUniquness() throws IOException {
    // given
    int numCandidates = 50;
    ElectionCandidate[] candidates = generateCandidates(numCandidates);

    // when

    // then
    List<String> ids =
        Arrays.asList(candidates).stream().map((e) -> e.getId()).collect(Collectors.toList());
    Set<String> uniqueIds = new HashSet<>(ids);
    assertThat(ids).hasSize(numCandidates);
    assertThat(uniqueIds).hasSize(numCandidates);
  }

  @Test
  void testTwoElectionTerms() throws IOException, InterruptedException {
    // given
    int numCandidates = 25;
    TaskManager tasks = new TaskManager();
    ElectionCandidate[] candidates = generateCandidates(numCandidates);
    tasks.start(Optional.of(this), true, candidates);
    waitForNewLeader();

    // when
    final Map<ElectionCandidate, ElectionCandidateState> statesAfterFirstElection =
        new HashMap<>(states);
    final ElectionCandidate firstLeader = findLeader(candidates);

    tasks.stop(firstLeader);
    waitForNewLeader();

    final Map<ElectionCandidate, ElectionCandidateState> statesAfterSecondElection =
        new HashMap<>(states);
    final ElectionCandidate secondLeader = findLeader(candidates);

    shutdownAllWithLeaderStoppingLast(tasks, candidates, secondLeader);
    Thread.sleep(100L);
    Map<ElectionCandidate, ElectionCandidateState> statesAfterShutdown = new HashMap<>(states);

    // then
    assertThat(statesAfterFirstElection).hasSize(numCandidates);
    assertThat(statesAfterSecondElection).hasSize(numCandidates);
    assertThat(statesAfterShutdown).hasSize(numCandidates);

    assertThat(firstLeader).isNotNull();
    assertThat(secondLeader).isNotNull();
    assertThat(firstLeader).isNotSameAs(secondLeader);

    assertThat(statesAfterFirstElection.get(firstLeader)).isEqualTo(LEADER);
    statesAfterFirstElection.remove(firstLeader);

    assertThat(statesAfterSecondElection.get(secondLeader)).isEqualTo(LEADER);
    statesAfterSecondElection.remove(secondLeader);

    for (ElectionCandidateState state : statesAfterFirstElection.values()) {
      assertThat(state).isAnyOf(INACTIVE, RUNNING_FOR_ELECTION);
    }
    for (ElectionCandidateState state : statesAfterSecondElection.values()) {
      assertThat(state).isAnyOf(INACTIVE, RUNNING_FOR_ELECTION);
    }
    for (ElectionCandidateState state : statesAfterShutdown.values()) {
      assertThat(state).isAnyOf(INACTIVE, RUNNING_FOR_ELECTION);
    }
  }

  @Test
  @EnabledOnOs({ LINUX })
  void testFailedLeaseFileWrite() {
    // given
    config.setSyncFileName("/dev/rtc0");
    ElectionCandidate candidate = new ElectionCandidate().configure(this, config);
    ElectionLease lease = new ElectionLease(candidate, DEFAULT_LEASE_TTL);

    candidate.state.set(LEADER);
    assertThat(candidate.isLeader()).isTrue();

    // when
    assertThrows(IOException.class, () -> candidate.writeLeaseFile(lease));

    // then
    assertThat(candidate.isLeader()).isFalse();
    assertThat(states.get(candidate)).isEqualTo(INACTIVE);
  }

  @Test
  void testMissingLease() {
    // given
    ElectionCandidate candidate = new ElectionCandidate().configure(this, null);
    candidate.state.set(LEADER);
    assertThat(candidate.isLeader()).isTrue();

    // when
    candidate.handleMissingLeaseFile(null);

    // then
    assertThat(candidate.isLeader()).isFalse();
    assertThat(states.get(candidate)).isEqualTo(INACTIVE);
  }

  @Test
  void testStolenLeaseFile() {
    // given
    final ElectionCandidate thief = new ElectionCandidate();
    final ElectionLease lease = new ElectionLease(thief, DEFAULT_LEASE_TTL);
    ElectionCandidate candidate1 = new ElectionCandidate().configure(this, null);
    ElectionCandidate candidate2 = new ElectionCandidate().configure(this, null);
    candidate1.state.set(LEADER);
    candidate2.state.set(RUNNING_FOR_ELECTION);
    assertThat(candidate1.isLeader()).isTrue();
    assertThat(candidate2.isLeader()).isFalse();

    // when
    candidate1.handleStolenLeaseFile(lease);
    candidate2.handleStolenLeaseFile(lease);

    // then
    assertThat(candidate1.isLeader()).isFalse();
    assertThat(candidate2.isLeader()).isFalse();
    assertThat(states.get(candidate1)).isEqualTo(INACTIVE);
    assertThat(states.get(candidate2)).isEqualTo(INACTIVE);
  }

  @Test
  void testExpiredLease() {
    // given
    ElectionCandidate candidate = new ElectionCandidate().configure(this, null);
    candidate.state.set(LEADER);
    assertThat(candidate.isLeader()).isTrue();
    ElectionLease lease = new ElectionLease(candidate.getId(), 50L, 100L);

    // when
    candidate.handleExpiredLeaseFile(lease);

    // then
    assertThat(candidate.isLeader()).isFalse();
    assertThat(states.get(candidate)).isEqualTo(INACTIVE);
  }

  ElectionCandidate[] generateCandidates(int n) throws IOException {
    File tmpFile = Files.createTempFile(getClass().getSimpleName(), null).toFile();
    tmpFile.deleteOnExit();
    config.setSyncFileName(tmpFile.getAbsolutePath());

    ElectionCandidate[] result = new ElectionCandidate[n];

    for (int i = 0; i < n; i++) {
      result[i] = new ElectionCandidate().configure(this, config);
      result[i].setName(result[i].getName() + "-" + (i + 1));
    }

    return result;
  }

  ElectionCandidate findLeader(ElectionCandidate[] candidates) {
    for (ElectionCandidate candidate : candidates) {
      if (candidate.isLeader()) {
        return candidate;
      }
    }

    throw new RuntimeException("No leader found!");
  }

  private void waitForNewLeader() throws InterruptedException {
    if (!waitForElection.tryAcquire(5, TimeUnit.SECONDS)) {
      throw new RuntimeException("Election took longer than 5 seconds; aborting!");
    }
  }

  private void shutdownAllWithLeaderStoppingLast(TaskManager tasks, ElectionCandidate[] candidates,
      ElectionCandidate leader) throws InterruptedException {
    for (ElectionCandidate c : candidates) {
      if (c != leader) {
        tasks.stop(c);
      }
    }
    boolean success = taskEndedSemaphore.tryAcquire(candidates.length - 1, 5, TimeUnit.SECONDS);
    if (!success) {
      throw new RuntimeException(
          "Non-leader candidate tasks took more than 5 seconds to go into state DEAD!");
    }

    tasks.stopAll();
  }

  @Override
  public synchronized void onStateChanged(ElectionCandidate candidate,
      ElectionCandidateState oldState, ElectionCandidateState newState) {
    states.put(candidate, newState);
    if (newState == LEADER) {
      waitForElection.release();
    }
  }

  @Override
  public void onTaskEnded(TaskEndedEvent event) {
    taskEndedSemaphore.release();
  }

}
