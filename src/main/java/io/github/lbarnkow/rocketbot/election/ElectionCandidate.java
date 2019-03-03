package io.github.lbarnkow.rocketbot.election;

import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.LEADER;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.taskmanager.Task;

public class ElectionCandidate extends Task {
	private final static Logger logger = LoggerFactory.getLogger(ElectionCandidate.class);

	static final long SLEEP_VARIANCE_MSEC = (long) (Math.random() * 100L);

	private final String id = UUID.randomUUID().toString();

	volatile ElectionCandidateState state = null;
	private ElectionCandidateListener listener;
	private ElectionConfiguration config;

	public ElectionCandidate configure(ElectionCandidateListener listener, ElectionConfiguration config) {
		this.listener = listener;
		this.config = config;
		return this;
	}

	public boolean isLeader() {
		return state == LEADER;
	}

	public String getId() {
		return id;
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		updateState(INACTIVE);

		try {
			while (true) {
				try {
					ElectionLease leaseFile = readLeaseFile();

					handleMissingLeaseFile(leaseFile);
					handleStolenLeaseFile(leaseFile);
					handleExpiredLeaseFile(leaseFile);

					if (state == LEADER || state == ACTIVATING) {
						leaseFile = refreshLease(leaseFile);
					} else {
						challengeLease(leaseFile);
					}
				} catch (IOException e) {
					logger.error("IOException occurred in LeaseManager!", e);
				}

				sleep();
			}
		} catch (InterruptedException e) {
			logger.info("Caught InterruptedException, withdrawing from election!");
		}

		try {
			releaseLease();
		} catch (IOException e) {
			logger.error("Unexpected IOException in LeaseManager!", e);
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
		if (state == LEADER || state == ACTIVATING) {
			Files.delete(config.getSyncFile().toPath());
			logger.info("Released lease (deleted lease file)!");
		}
		updateState(INACTIVE);
	}

	private void challengeLease(ElectionLease leaseFile) throws IOException {
		if (leaseFile == null || leaseFile.isExpired()) {
			ElectionLease newLease = new ElectionLease(id, config.getLeaseTimeToLive());
			writeLeaseFile(newLease);

			logger.info("Running for election w/ id '{}'", id);

			updateState(ACTIVATING);
		}
	}

	private ElectionLease refreshLease(ElectionLease oldLease) throws IOException {
		ElectionLease newLease = new ElectionLease(oldLease, config.getLeaseTimeToLive());
		writeLeaseFile(newLease);

		if (state == ACTIVATING) {
			logger.info("Won election w/ id '{}'! Promoted to state '{}'!", id, LEADER);
		}
		updateState(LEADER);

		return newLease;
	}

	private void updateState(ElectionCandidateState newState) {
		if (state == newState)
			return;

		ElectionCandidateState oldState = state;
		state = newState;
		logger.trace("Switched to state '{}'.", state.toString());

		listener.onStateChanged(this, oldState, newState);
	}

	private void sleep() throws InterruptedException {
		long sleepTime = SLEEP_VARIANCE_MSEC;

		if (state == LEADER || state == ACTIVATING) {
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

		if (state == LEADER || state == ACTIVATING) {
			logger.error("Lease file missing even though my state was '{}'! Reverting back to '{}'.", state, INACTIVE);
		}

		updateState(INACTIVE);
	}

	void handleStolenLeaseFile(ElectionLease leaseFile) {
		if (leaseFile == null) {
			return;
		}

		if (state != LEADER && state != ACTIVATING)
			return;
		if (leaseFile.isOwnedBy(id))
			return;

		if (state == LEADER) {
			logger.error("Lease file stolen by '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
		} else if (state == ACTIVATING) {
			logger.info("Lost election to '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
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

		if (state == LEADER || state == ACTIVATING) {
			logger.error("Lease file expired during my term (state '{}')! Reverting back to '{}'.", state, INACTIVE);
		}

		updateState(INACTIVE);
	}
}
