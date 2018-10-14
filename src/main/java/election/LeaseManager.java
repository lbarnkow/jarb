package election;

import static election.State.ACTIVATING;
import static election.State.INACTIVE;
import static election.State.LEADER;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaseManager implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(LeaseManager.class);

	private final String id = UUID.randomUUID().toString();

	private volatile boolean alive = true;
	private volatile State state = INACTIVE;

	private final LeaseManagerListener listener;

	public LeaseManager(LeaseManagerListener listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		while (alive) {
			try {

				Lease leaseFile = readLeaseFile();

				handleMissingLeaseFile();
				checkForStolenLeaseFile(leaseFile);

				if (state == LEADER || state == ACTIVATING) {
					boolean refreshResult = refreshLease(leaseFile);
					if (refreshResult)
						updateState(LEADER);
					else
						updateState(INACTIVE);

				} else {
					boolean challengeResult = challengeLease(leaseFile);
					if (challengeResult)
						updateState(ACTIVATING);
				}

			} catch (IOException e) {
				logger.error("Exception in LeaseManager!", e);
			}

			sleep();
		}

		releaseLease();
	}

	private Lease readLeaseFile() throws IOException {
		FileUtils.readFileToString(Config.SYNC_FILE, "utf-8");
		return null;
	}

	private void writeLeaseFile(Lease lease) {
		// TODO
	}

	private void releaseLease() {
		if (state == LEADER || state == ACTIVATING) {
			// TODO: simply delete file.
			logger.info("Released lease (deleted lease file)!");
		}
		updateState(INACTIVE);
	}

	private boolean challengeLease(Lease leaseFile) {
		if (leaseFile != null && !leaseFile.isExpired())
			return false;

		Lease newLease = new Lease(id);
		writeLeaseFile(newLease);

		logger.info("Challenging lease w/ id '{}'", id);

		return true;
	}

	private boolean refreshLease(Lease leaseFile) {
		// TODO Auto-generated method stub

		return false;
	}

	private void updateState(State newState) {
		if (state == newState)
			return;

		state = newState;
		logger.trace("Switched to state '{}'.", state.toString());

		listener.onStateChanged(state);
	}

	private void sleep() {
		long sleepTime = Config.SLEEP_VARIANCE_MSEC;

		if (state == LEADER || state == ACTIVATING)
			sleepTime += Config.LEASE_REFRESH_MSEC;
		else
			sleepTime += Config.LEASE_CHALLENGE_MSEC;

		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			logger.error("Sleep interrupted!");
		}
	}

	private void checkForStolenLeaseFile(Lease leaseFile) {
		if (state != LEADER && state != ACTIVATING)
			return;
		if (leaseFile.isLeader(id))
			return;

		if (state == LEADER) {
			logger.error("Lease file stolen by '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
			updateState(INACTIVE);
		} else if (state == ACTIVATING) {
			logger.info("Lost lease election to '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
			updateState(INACTIVE);
		}
	}

	private void handleMissingLeaseFile() {
		if (state == LEADER || state == ACTIVATING)
			logger.error("Lease file missing even though my state was '{}'! Reverting back to '{}'.", state, INACTIVE);
		updateState(INACTIVE);
	}
}
