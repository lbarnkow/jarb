package bot.rocketchat;

import static bot.rocketchat.websocket.MessageTypes.ADDED;
import static bot.rocketchat.websocket.MessageTypes.CONNECTED;
import static bot.rocketchat.websocket.MessageTypes.PING;
import static bot.rocketchat.websocket.MessageTypes.RESULT;
import static bot.rocketchat.websocket.MessageTypes.UPDATED;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.ConnectionInfo;
import bot.rocketchat.rest.RestClient;
import bot.rocketchat.tasks.LoginTask;
import bot.rocketchat.util.ObjectHolder;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.WebsocketClientListener;
import bot.rocketchat.websocket.messages.Base;
import bot.rocketchat.websocket.messages.RecConnected;
import bot.rocketchat.websocket.messages.RecLogin;
import bot.rocketchat.websocket.messages.RecLogin.Result;
import bot.rocketchat.websocket.messages.RecWithId;
import bot.rocketchat.websocket.messages.SendConnect;
import bot.rocketchat.websocket.messages.SendPong;

public class RocketChatClient implements WebsocketClientListener {

	private static final long LOGIN_THREAD_SLEEP_TIME_MILLIS = 60000L;

	private static final Logger logger = LoggerFactory.getLogger(RocketChatClient.class);

	private final ConnectionInfo conInfo;
	private final RocketChatClientListener listener;
	private WebsocketClient wsClient;
	private RestClient rsClient;

	private final Semaphore syncWaitForConnected = new Semaphore(0);
	private final Semaphore syncWaitForLoggedIn = new Semaphore(0);
	private Thread loginThread;
	private ObjectHolder<RecLogin.Result> loginTokenHolder = new ObjectHolder<>();

	private State state = State.DISCONNECTED;

	public RocketChatClient(ConnectionInfo conInfo, RocketChatClientListener listener) {
		this.conInfo = conInfo;
		this.listener = listener;
	}

	public void start() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		state = State.CONNECTED;
		wsClient = new WebsocketClient(conInfo, this);

		wsClient.sendMessage(new SendConnect());
		syncWaitForConnected.acquire();

		syncWaitForLoggedIn.drainPermits();
		loginThread = new Thread(new LoginTask(conInfo, wsClient, LOGIN_THREAD_SLEEP_TIME_MILLIS));
		loginThread.start();
		syncWaitForLoggedIn.acquire();

		// TODO
		rsClient = new RestClient(conInfo, loginTokenHolder);
		List<String> roomIds = rsClient.getRoomIds();
		System.out.println(roomIds);

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
		// TODO: reset rsClient?
		loginTokenHolder.reset();
		state = State.DISCONNECTED;
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

		if (PING.matches(entity)) {
			handleMessagePing();
		} else if (CONNECTED.matches(entity)) {
			handleMessageConnected(message);
		} else if (RESULT.matches(entity)) {
			handleMessageResult(message);
		} else if (UPDATED.matches(entity)) {
			// Do nothing
		} else if (ADDED.matches(entity)) {
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

		if (id.startsWith(LoginTask.ID_PREFIX)) {
			handleLoginResult(message);
		} else {
			// Handle other "results"
			logger.info("Unhandled: {}!", message);

		}
	}

	private void handleLoginResult(String message) {
		RecLogin login = RecLogin.parse(message);
		Result loginToken = login.getResult();
		loginTokenHolder.set(loginToken);

		long expires = Long.parseLong(loginToken.getTokenExpires().get$date());
		logger.info("Logged in as '{}', token expires at '{}'!", conInfo.getUsername(), Instant.ofEpochMilli(expires));

		syncWaitForLoggedIn.release();
	}

	public State getState() {
		return state;
	}

	public static enum State {
		DISCONNECTED, CONNECTED
	}

}