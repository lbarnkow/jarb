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

import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.RUNNING_FOR_ELECTION;

import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/**
 * An <code>ElectionCandidate</code> represents one member in a group of
 * processes where only one member at a time shall be active. This
 * implementation uses a shared file on the file system (might be remote via
 * NFS). Every process reads that file to determine if an active leader is
 * around (file exists, recorded lease has not expired). If there is no active
 * leader this process will generate a new <code>ElectionLease</code> and write
 * it to the shared file. If a after a configurable time the file still shows
 * this process as active leader, then leadership role is assumed. Configurable
 * lease durations are rather short, so this process has to refresh the lease
 * regularly.
 *
 * @author lbarnkow
 */
@Slf4j
public class ElectionCandidate extends AbstractBaseTask {

  /**
   * Per instance sleep time variation. This is intended to help with
   * race-conditions where multiple processes plan on pausing at the same time for
   * the same amount of time. This random variation per instance will make their
   * sleep times every so slightly different.
   */
  private final transient long sleepVarianceMsec = (long) (Math.random() * 100L);

  /**
   * Random unique id of this <code>ElectionCandidate</code>.
   */
  private final transient String id = UUID.randomUUID().toString();

  /**
   * The current <code>ElectionCandidateState</code> of this candidate.
   */
  transient AtomicReference<ElectionCandidateState> state =
      new AtomicReference<ElectionCandidateState>(null);

  /**
   * A listener to inform on changes of state changes for this candidate.
   */
  private transient ElectionCandidateListener listener;

  /**
   * Externally supplied configuration.
   */
  private transient ElectionConfiguration config;

  /**
   * Post-construction initialization by the user of this instance. Since this
   * instance will most likely be created by the dependency injection framework,
   * this method is used to parameterize it further.
   *
   * @param listener the listener to inform about state changes
   * @param config   the parsed configuration
   * @return this instance
   */
  public ElectionCandidate configure(ElectionCandidateListener listener,
      ElectionConfiguration config) {
    this.listener = listener;
    this.config = config;
    return this;
  }

  /**
   * Returns whether or not this candidate is currently in leadership state.
   *
   * @return <code>true</code> if this candidate is in leadership state;
   *         <code>false</code> otherwise
   */
  public boolean isLeader() {
    return state.get() == LEADER;
  }

  /**
   * Gets the unique id of this candidate.
   *
   * @return the unique id
   */
  public String getId() {
    return id;
  }

  @Override
  public void runTask() throws Exception {
    updateState(INACTIVE);

    try {
      while (true) {
        try {
          ElectionLease leaseFile = readLeaseFile();

          handleMissingLeaseFile(leaseFile);
          handleStolenLeaseFile(leaseFile);
          handleExpiredLeaseFile(leaseFile);

          ElectionCandidateState state = this.state.get();
          if (state == LEADER || state == RUNNING_FOR_ELECTION) {
            leaseFile = refreshLease(leaseFile);
          } else {
            challengeLease(leaseFile);
          }
        } catch (IOException e) {
          log.error("IOException occurred in LeaseManager!", e);
        }

        sleep();
      }
    } catch (InterruptedException e) {
      log.info("Caught InterruptedException, withdrawing from election!");
    }

    try {
      releaseLease();
    } catch (IOException e) {
      log.error("Unexpected IOException in LeaseManager!", e);
    }
  }

  private ElectionLease readLeaseFile() throws IOException {
    ElectionLease lease = ElectionLease.load(config.getSyncFile());

    if (lease == null) {
      updateState(INACTIVE);
    }

    return lease;
  }

  void writeLeaseFile(ElectionLease lease) throws IOException {
    try {
      ElectionLease.save(lease, config.getSyncFile());
    } catch (IOException e) {
      updateState(INACTIVE);
      throw e;
    }
  }

  private void releaseLease() throws IOException {
    ElectionCandidateState state = this.state.get();
    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      updateState(INACTIVE);
      Files.delete(config.getSyncFile().toPath());
      log.info("Released lease (deleted lease file)!");
    }
  }

  private void challengeLease(ElectionLease leaseFile) throws IOException {
    if (leaseFile == null || leaseFile.isExpired()) {
      ElectionLease newLease = new ElectionLease(this, config.getLeaseTimeToLive());
      writeLeaseFile(newLease);

      log.info("Running for election w/ id '{}'", id);

      updateState(RUNNING_FOR_ELECTION);
    }
  }

  private ElectionLease refreshLease(ElectionLease oldLease) throws IOException {
    ElectionLease newLease = new ElectionLease(oldLease, config.getLeaseTimeToLive());
    writeLeaseFile(newLease);

    if (state.get() == RUNNING_FOR_ELECTION) {
      log.info("Won election w/ id '{}'! Promoted to state '{}'!", id, LEADER);
    }
    updateState(LEADER);

    return newLease;
  }

  private void updateState(ElectionCandidateState newState) {
    ElectionCandidateState oldState = state.getAndSet(newState);
    if (oldState == newState) {
      return;
    }

    log.trace("Switched to state '{}'.", newState.toString());

    listener.onStateChanged(this, oldState, newState);
  }

  private void sleep() throws InterruptedException {
    long sleepTime = sleepVarianceMsec;

    ElectionCandidateState state = this.state.get();
    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      sleepTime += config.getLeaseRefreshInterval();
    } else {
      sleepTime += config.getLeaseChallengeInterval();
    }

    Thread.sleep(sleepTime);
  }

  void handleMissingLeaseFile(ElectionLease leaseFile) {
    if (leaseFile != null) {
      return;
    }

    ElectionCandidateState state = this.state.get();
    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      log.error("Lease file missing even though my state was '{}'! Reverting back to '{}'.", state,
          INACTIVE);
    }

    updateState(INACTIVE);
  }

  void handleStolenLeaseFile(ElectionLease leaseFile) {
    if (leaseFile == null) {
      return;
    }

    ElectionCandidateState state = this.state.get();

    if (state != LEADER && state != RUNNING_FOR_ELECTION) {
      return;
    }
    if (leaseFile.isOwnedBy(this)) {
      return;
    }

    if (state == LEADER) {
      log.error("Lease file stolen by '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(),
          INACTIVE);
    } else {
      log.info("Lost election to '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
    }

    updateState(INACTIVE);
  }

  void handleExpiredLeaseFile(ElectionLease leaseFile) {
    if (leaseFile == null) {
      return;
    }

    if (!leaseFile.isExpired()) {
      return;
    }

    ElectionCandidateState state = this.state.get();
    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      log.error("Lease file expired during my term (state '{}')! Reverting back to '{}'.", state,
          INACTIVE);
    }

    updateState(INACTIVE);
  }
}
