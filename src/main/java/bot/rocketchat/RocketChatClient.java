package bot.rocketchat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.ConnectionInfo;
import bot.rocketchat.messages.Base;
import bot.rocketchat.messages.SendConnect;
import bot.rocketchat.messages.SendLogin;
import bot.rocketchat.messages.SendPong;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.WebsocketClientListener;

public class RocketChatClient implements WebsocketClientListener {

	private static final long LOGIN_THREAD_SLEEP_TIME_MILLIS = 60000L;

	private static final Logger logger = LoggerFactory.getLogger(RocketChatClient.class);

	private static final String MSG_PING = "ping";
	private static final String MSG_CONNECTED = "connected";
	private static final String MSG_RESULT = "result";

	private final ConnectionInfo conInfo;
	private final RocketChatClientListener listener;
	private WebsocketClient wsClient;

	private final Semaphore syncWaitForConnected = new Semaphore(0);
	private Thread loginThread;

	public RocketChatClient(ConnectionInfo conInfo, RocketChatClientListener listener) {
		this.conInfo = conInfo;
		this.listener = listener;
	}

	public void start() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		// TODO
		wsClient = new WebsocketClient(conInfo.getServerUrl(), this);

		wsClient.sendMessage(new SendConnect());
		syncWaitForConnected.acquire();

		loginThread = new Thread(new LoginSender(conInfo, wsClient, LOGIN_THREAD_SLEEP_TIME_MILLIS));
		loginThread.start();

		// start login-thread, wait for logged in.

		// start room / subscription thread, wait for first result
		//// refresh public-rooms
		//// get subscriptions (public, private, direct)
		//// join new rooms

		// catch-up
		//// check for unread messages in all subscriptions
		//// handle unread messages and mark as read

		// start real-time subscriptions
	}

	public void stop() {
		// TODO
		// stop subscriptions
		// stop login-thread (+ logout?)
		// close websocket
	}

	@Override
	public void onWebsocketClose(boolean initiatedByClient) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onWebsocketMessage(String message) {
		Base entity = Base.parse(message);

		if (MSG_PING.equals(entity.getMsg())) {
			wsClient.sendMessage(new SendPong());

		} else if (MSG_CONNECTED.equals(entity.getMsg())) {
			// TODO
			return;

		} else if (MSG_RESULT.equals(entity.getMsg())) {
			// TODO
			return;

		} else {
			logger.warn("Unhandled message received in class '{}': '{}'!", getClass().getSimpleName(), message);
		}
	}

	private static class LoginSender implements Runnable {
		private final ConnectionInfo conInfo;
		private final WebsocketClient wsClient;
		private final long sleepTimeMillis;

		public LoginSender(ConnectionInfo conInfo, WebsocketClient wsClient, long sleepTimeMillis) {
			this.conInfo = conInfo;
			this.wsClient = wsClient;
			this.sleepTimeMillis = sleepTimeMillis;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				wsClient.sendMessage(new SendLogin(conInfo.getUsername(), conInfo.getPassword()));
				try {
					Thread.sleep(sleepTimeMillis);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}