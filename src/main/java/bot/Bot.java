package bot;

import static bot.rocketchat.RocketChatClient.State.CONNECTED;
import static election.State.LEADER;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.rocketchat.Message;
import bot.rocketchat.RocketChatClient;
import bot.rocketchat.RocketChatClientListener;
import election.LeaseManager;
import election.LeaseManagerListener;
import election.State;

public class Bot extends CommonBase implements Runnable, LeaseManagerListener, RocketChatClientListener {
	private static final Logger logger = LoggerFactory.getLogger(Bot.class);

	private final RocketChatClient rcClient;
	private final LeaseManager leaseManager = new LeaseManager(this);;
	private final Semaphore syncWaitForLease = new Semaphore(0);
	private final Semaphore syncLostLease = new Semaphore(0);
	private volatile boolean alive = false;
	private Thread thread = null;

	// TODO:
//	String regex = "^(?:(?:BOTNAME\\s.*)|(?:@BOTNAME\\s.*))";
//	regex = regex.replaceAll("BOTNAME", "demobot");
//
//	this.pattern = Pattern.compile(regex);

	@Inject
	private Bot(ConnectionInfo conInfo, RocketChatClient rcClient) {
		this.rcClient = rcClient;
		rcClient.setListener(this);
	}

	@Override
	public Message onRocketChatClientMessage(Message message) {
		System.out.println(message);
		return null;
	}

	@Override
	public void run() {
		alive = true;
		thread = Thread.currentThread();

		startLeaseManager();

		try {
			while (alive) {
				if (Thread.interrupted())
					throw new InterruptedException();

				syncWaitForLease.acquire();
				rcClient.start();

				syncLostLease.acquire();
				rcClient.stop();
			}
		} catch (InterruptedException e) {
			logger.error("Caught '{}', shutting down!", e.getClass().getSimpleName());
		} catch (URISyntaxException | DeploymentException | IOException e) {
			logger.error("Caught unexpected exception, shutting down!", e);
		}

		try {
			if (rcClient.getState() == CONNECTED)
				rcClient.stop();
		} catch (IOException e) {
			logger.error("Error stopping {}!", rcClient.getClass().getSimpleName(), e);
		}

		leaseManager.stop();
	}

	private void startLeaseManager() {
		new Thread(leaseManager).start();
	}

	public void stop() {
		alive = false;
		if (thread != null)
			thread.interrupt();
	}

	@Override
	public void onStateChanged(String id, State oldState, State newState) {
		if (newState == LEADER)
			syncWaitForLease.release();
		else if (oldState == LEADER)
			syncLostLease.release();
	}

	@Override
	public void onRocketChatClientClose(boolean initiatedByClient) {
		stop();
	}
}
