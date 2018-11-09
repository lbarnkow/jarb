package bot.rocketchat.tasks;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.CommonBase;
import bot.ConnectionInfo;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.messages.SendLogin;

public class LoginTask extends CommonBase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(LoginTask.class);

	public static final String ID_PREFIX = UUID.randomUUID().toString() + "-";

	private final ConnectionInfo conInfo;
	private final WebsocketClient wsClient;
	private final long sleepTimeMillis;
	private final AtomicLong counter = new AtomicLong();

	public LoginTask(ConnectionInfo conInfo, WebsocketClient wsClient, long sleepTimeMillis) {
		this.conInfo = conInfo;
		this.wsClient = wsClient;
		this.sleepTimeMillis = sleepTimeMillis;
	}

	@Override
	public void run() {
		logger.debug("Login / token refresh thread started.");

		while (!Thread.interrupted()) {
			long index = counter.incrementAndGet();
			String id = ID_PREFIX + index;
			wsClient.sendMessage(new SendLogin(id, conInfo.getUsername(), conInfo.getPassword()));
			try {
				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				break;
			}
		}

		// there's seemingly no logout message in the real-time api for now.
		logger.debug("Login / token refresh thread stopped.");
	}
}