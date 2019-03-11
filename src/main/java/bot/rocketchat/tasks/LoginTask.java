package bot.rocketchat.tasks;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.CommonBase;
import bot.ConnectionInfo;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.messages.WebsocketMessageProvider;
import bot.rocketchat.websocket.messages.out.SendLogin;

public class LoginTask extends CommonBase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(LoginTask.class);

	public static final String ID_PREFIX = UUID.randomUUID().toString() + "-";

	private boolean initialized;
	private volatile boolean alive;
	private Thread myThread;

	@Inject
	private ConnectionInfo conInfo;
	private WebsocketClient wsClient;
	private long sleepTimeMillis;

	@Inject
	private AtomicLong counter;

	@Inject
	private WebsocketMessageProvider wsMessages;

	LoginTask() {
	}

	public void initialize(WebsocketClient wsClient, long sleepTimeMillis) {
		if (this.initialized)
			throw new IllegalStateException("LoginTask already initialized!");

		this.wsClient = wsClient;
		this.sleepTimeMillis = sleepTimeMillis;
		this.initialized = true;
	}

	@Override
	public void run() {
		logger.debug("Login / token refresh thread started.");
		myThread = Thread.currentThread();
		alive = true;

		while (alive) {
			long index = counter.incrementAndGet();
			String id = ID_PREFIX + index;
			SendLogin sendLoginMsg = wsMessages.get(SendLogin.class);
			sendLoginMsg.initialize(id, conInfo.getUsername(), conInfo.getPassword());
			wsClient.sendMessage(sendLoginMsg);
			try {
				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				break;
			}
		}

		// there's seemingly no logout message in the real-time api for now.
		logger.debug("Login / token refresh thread stopped.");
	}

	public void stop() {
		alive = false;
		myThread.interrupt();

		while (myThread.isAlive()) {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
			}
		}
	}
}