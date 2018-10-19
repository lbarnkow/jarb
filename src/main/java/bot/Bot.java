package bot;

import static election.State.LEADER;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import election.LeaseManager;
import election.LeaseManagerListener;
import election.State;

public class Bot implements Runnable, LeaseManagerListener, MessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(Bot.class);

	private final LeaseManager leaseManager = new LeaseManager(this);;
	private final ConnectionInfo conInfo;
	private final Semaphore syncWaitForLease = new Semaphore(0);
	private final Semaphore syncLostLease = new Semaphore(0);
	private volatile boolean alive = false;

	public Bot(ConnectionInfo conInfo) {
		this.conInfo = conInfo;
	}

	@Override
	public void run() {
		RocketChatClient rcClient = new RocketChatClient(conInfo, this);

		alive = true;
		startLeaseManager();

		try {
			while (alive) {
				if (Thread.interrupted())
					throw new InterruptedException();

				syncWaitForLease.acquire();
				if (!leaseManager.isAlive() || !leaseManager.isLeader())
					continue;
			}

			rcClient.start();
			syncLostLease.acquire();
		} catch (InterruptedException e) {
		}

		rcClient.stop();
		leaseManager.stop();
	}

	private void startLeaseManager() {
		new Thread(leaseManager).start();
	}

	public void shutdown() {
		alive = false;
	}

	@Override
	public void onStateChanged(String id, State oldState, State newState) {
		if (newState == LEADER)
			syncWaitForLease.release();
		else if (oldState == LEADER)
			syncLostLease.release();
	}

	@Override
	public Message handle(Message message) {
		logger.info(message.toString());
		return null;
	}
}