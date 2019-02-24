package election;

import static election.State.ACTIVATING;
import static election.State.INACTIVE;
import static election.State.LEADER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class LeaseManager implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(LeaseManager.class);

	private final static int MAX_MOVE_RETRIES = 5;

	private final static Gson gson = new Gson();

	private final String id = UUID.randomUUID().toString();

	private volatile boolean alive = false;
	private volatile State state = INACTIVE;

	private LeaseManagerListener listener;

	@Inject
	private Provider<Lease> leaseProvider;

	LeaseManager() {
	}

	public void setListener(LeaseManagerListener listener) {
		this.listener = listener;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isLeader() {
		return state == LEADER;
	}

	public String getId() {
		return id;
	}

	@Override
	public void run() {
		String previousLeader = "";
		alive = true;
		state = INACTIVE;

		while (alive) {
			try {
				Lease leaseFile = readLeaseFile();

				if (leaseFile != null && !leaseFile.getLeaderId().equals(previousLeader)) {
					previousLeader = leaseFile.getLeaderId();
					logger.info("Lease is held by id '{}'", leaseFile.getLeaderId());
				}

				handleMissingLeaseFile(leaseFile);
				handleStolenLeaseFile(leaseFile);
				handleExpiredLeaseFile(leaseFile);

				if (state == LEADER || state == ACTIVATING) {
					refreshLease(leaseFile);
					if (state == ACTIVATING)
						logger.info("Acquired lease w/ id '{}'! Promoted to state '{}'!", id, LEADER);
					updateState(LEADER);
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

		try {
			releaseLease();
		} catch (IOException e) {
			logger.error("Exception in LeaseManager!", e);
		}
	}

	public void stop() {
		alive = false;
		while (state != INACTIVE) {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
			}
		}
	}

	private Lease readLeaseFile() throws IOException {
		IOException lastException = null;
		for (int i = 0; i < MAX_MOVE_RETRIES; i++)
			try {
				String json = FileUtils.readFileToString(Config.SYNC_FILE, "utf-8");
				return gson.fromJson(json, Lease.class);
			} catch (IOException e) {
				lastException = e;
			}

		updateState(INACTIVE);
		if (lastException instanceof FileNotFoundException)
			return null;

		throw lastException;
	}

	private void writeLeaseFile(Lease lease) throws IOException {
		String json = gson.toJson(lease);
		Path tmp = Files.createTempFile("chatbot", null);
		FileUtils.writeStringToFile(tmp.toFile(), json, Charset.forName("utf-8"));

		IOException lastException = null;
		for (int i = 0; i < MAX_MOVE_RETRIES; i++) {
			if (lease.isExpired()) {
				updateState(INACTIVE);
				throw new RuntimeException("Lease '" + id
						+ "' expired while trying to write it to storage! Reverting back to '" + state + "'!");
			}

			try {
				Files.move(tmp, Config.SYNC_FILE.toPath(), REPLACE_EXISTING);
				return;
			} catch (IOException e) {
				lastException = e;
			}
		}
		updateState(INACTIVE);
		throw lastException;
	}

	private void releaseLease() throws IOException {
		if (state == LEADER || state == ACTIVATING) {
			Files.delete(Config.SYNC_FILE.toPath());
			logger.info("Released lease (deleted lease file)!");
		}
		updateState(INACTIVE);
	}

	private boolean challengeLease(Lease leaseFile) throws IOException {
		if (leaseFile != null && !leaseFile.isExpired())
			return false;

		Lease newLease = leaseProvider.get();
		newLease.setLeaderId(id);
		writeLeaseFile(newLease);

		logger.info("Challenging lease w/ id '{}'", id);

		return true;
	}

	private void refreshLease(Lease leaseFile) throws IOException {
		leaseFile.refreshExpiration();
		writeLeaseFile(leaseFile);
	}

	private void updateState(State newState) {
		if (state == newState)
			return;

		State oldState = state;
		state = newState;
		logger.trace("Switched to state '{}'.", state.toString());

		listener.onStateChanged(id, oldState, newState);
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

	private void handleMissingLeaseFile(Lease leaseFile) {
		if (leaseFile != null)
			return;

		if (state == LEADER || state == ACTIVATING)
			logger.error("Lease file missing even though my state was '{}'! Reverting back to '{}'.", state, INACTIVE);

		updateState(INACTIVE);
	}

	private void handleStolenLeaseFile(Lease leaseFile) {
		if (leaseFile == null)
			return;

		if (state != LEADER && state != ACTIVATING)
			return;
		if (leaseFile.isLeader(id))
			return;

		if (state == LEADER)
			logger.error("Lease file stolen by '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
		else if (state == ACTIVATING)
			logger.info("Lost lease election to '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
		updateState(INACTIVE);
	}

	private void handleExpiredLeaseFile(Lease leaseFile) {
		if (leaseFile == null)
			return;

		if (!leaseFile.isExpired())
			return;

		if (state == LEADER || state == ACTIVATING)
			logger.error("Lease file expired in state '{}'! Reverting back to '{}'.", state, INACTIVE);

		updateState(INACTIVE);
	}
}
