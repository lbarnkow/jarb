package bot.rocketchat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.Semaphore;

import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.ConnectionInfo;
import bot.rocketchat.messages.Base;
import bot.rocketchat.messages.RecConnected;
import bot.rocketchat.messages.RecLogin;
import bot.rocketchat.messages.RecWithId;
import bot.rocketchat.messages.SendConnect;
import bot.rocketchat.messages.SendPong;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.WebsocketClientListener;

public class RocketChatClient implements WebsocketClientListener {

	private static final long LOGIN_THREAD_SLEEP_TIME_MILLIS = 60000L;

	private static final Logger logger = LoggerFactory.getLogger(RocketChatClient.class);

	private static final String MSG_PING = "ping";
	private static final String MSG_CONNECTED = "connected";
	private static final String MSG_RESULT = "result";
	private static final String MSG_ADDED = "added";
	private static final String MSG_UPDATED = "updated";

	private final ConnectionInfo conInfo;
	private final RocketChatClientListener listener;
	private WebsocketClient wsClient;

	private final Semaphore syncWaitForConnected = new Semaphore(0);
	private final Semaphore syncWaitForLoggedIn = new Semaphore(0);
	private Thread loginThread;

	public RocketChatClient(ConnectionInfo conInfo, RocketChatClientListener listener) {
		this.conInfo = conInfo;
		this.listener = listener;
	}

	public void start() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		wsClient = new WebsocketClient(conInfo.getServerUrl(), this);

		wsClient.sendMessage(new SendConnect());
		syncWaitForConnected.acquire();

		loginThread = new Thread(new LoginSender(conInfo, wsClient, LOGIN_THREAD_SLEEP_TIME_MILLIS));
		loginThread.start();
		syncWaitForLoggedIn.acquire();

		// TODO
		// start room / subscription thread, wait for first result
		//// refresh public-rooms
		//// get subscriptions (public, private, direct)
		//// join new rooms

		// catch-up
		//// check for unread messages in all subscriptions
		//// handle unread messages and mark as read

		// start real-time subscriptions
	}

	public void stop() throws IOException {
		// TODO
		// stop subscriptions

		loginThread.interrupt();
		wsClient.close();
	}

	@Override
	public void onWebsocketClose(boolean initiatedByClient) {
		logger.debug("WebsocketClient closed the session.");

		loginThread.interrupt();
		listener.onRocketChatClientClose(initiatedByClient);
	}

	@Override
	public void onWebsocketMessage(String message) {
		Base entity = Base.parse(message);

		if (MSG_PING.equals(entity.getMsg())) {
			handleMessagePing();

		} else if (MSG_CONNECTED.equals(entity.getMsg())) {
			handleMessageConnected(message);

		} else if (MSG_RESULT.equals(entity.getMsg())) {
			handleMessageResult(message);

		} else if (MSG_UPDATED.equals(entity.getMsg())) {
			// Do nothing

		} else if (MSG_ADDED.equals(entity.getMsg())) {
			// Do nothing

		} else {
			logger.warn("Unhandled message received in class '{}': '{}'!", getClass().getSimpleName(), message);
		}
	}

	private void handleMessagePing() {
		wsClient.sendMessage(new SendPong());
	}

	private void handleMessageConnected(String message) {
		RecConnected connected = RecConnected.parse(message);
		logger.info("Connected and started session '{}'.", connected.getSession());

		syncWaitForConnected.release();
	}

	private void handleMessageResult(String message) {
		RecWithId recWithId = RecWithId.parse(message);
		String id = recWithId.getId();

		if (id.startsWith(LoginSender.ID_PREFIX)) {
			RecLogin login = RecLogin.parse(message);
			long expires = Long.parseLong(login.getResult().getTokenExpires().get$date());
			logger.info("Logged in as '{}', token expires at '{}'!", conInfo.getUsername(),
					Instant.ofEpochMilli(expires));
			syncWaitForLoggedIn.release();

		} else {
			// Handle other "results"
		}
	}
}