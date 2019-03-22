package io.github.lbarnkow.jarb.election;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.ACTIVATING;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.LINUX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import io.github.lbarnkow.jarb.taskmanager.TaskManager;

class ElectionCandidateTest implements ElectionCandidateListener {

	private static final long DEFAULT_LEASE_TTL = 1000L;

	private final ElectionConfiguration config = new ElectionConfiguration();

	private Map<ElectionCandidate, ElectionCandidateState> states = new HashMap<>();

	@Test
	void testIdUniquness() throws IOException {
		// given
		int numCandidates = 50;
		ElectionCandidate[] candidates = generateCandidates(numCandidates);

		// when

		// then
		List<String> ids = Arrays.asList(candidates).stream().map((e) -> e.getId()).collect(Collectors.toList());
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
		tasks.start(candidates);
		Thread.sleep(1000L);

		// when
		Map<ElectionCandidate, ElectionCandidateState> statesAfterFirstElection = new HashMap<>(states);
		ElectionCandidate firstLeader = findLeader(candidates);

		tasks.stop(firstLeader);
		Thread.sleep(1000L);

		Map<ElectionCandidate, ElectionCandidateState> statesAfterSecondElection = new HashMap<>(states);
		ElectionCandidate secondLeader = findLeader(candidates);

		tasks.stopAll();
		Thread.sleep(50L);
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
			assertThat(state).isEqualTo(INACTIVE);
		}
		for (ElectionCandidateState state : statesAfterSecondElection.values()) {
			assertThat(state).isEqualTo(INACTIVE);
		}
		for (ElectionCandidateState state : statesAfterShutdown.values()) {
			assertThat(state).isEqualTo(INACTIVE);
		}
	}

	@Test
	@EnabledOnOs({ LINUX })
	void testFailedLeaseFileWrite() {
		// given
		config.setSyncFileName("/dev/rtc0");
		ElectionCandidate candidate = new ElectionCandidate().configure(this, config);
		ElectionLease lease = new ElectionLease(candidate.getId(), DEFAULT_LEASE_TTL);

		candidate.state = LEADER;
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
		candidate.state = LEADER;
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
		ElectionLease lease = new ElectionLease("testing", DEFAULT_LEASE_TTL);
		ElectionCandidate candidate1 = new ElectionCandidate().configure(this, null);
		ElectionCandidate candidate2 = new ElectionCandidate().configure(this, null);
		candidate1.state = LEADER;
		candidate2.state = ACTIVATING;
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
		candidate.state = LEADER;
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

	@Override
	public synchronized void onStateChanged(ElectionCandidate candidate, ElectionCandidateState oldState,
			ElectionCandidateState newState) {
		states.put(candidate, newState);
	}

}
