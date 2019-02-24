package bot.rocketchat;

import static bot.rocketchat.websocket.RealTimeMessageTypes.ADDED;
import static bot.rocketchat.websocket.RealTimeMessageTypes.CHANGED;
import static bot.rocketchat.websocket.RealTimeMessageTypes.CONNECTED;
import static bot.rocketchat.websocket.RealTimeMessageTypes.PING;
import static bot.rocketchat.websocket.RealTimeMessageTypes.READY;
import static bot.rocketchat.websocket.RealTimeMessageTypes.RESULT;
import static bot.rocketchat.websocket.RealTimeMessageTypes.UPDATED;
import static bot.rocketchat.websocket.messages.in.RecChangedStreamRoomMessages.COLLECTION;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import bot.CommonBase;
import bot.ConnectionInfo;
import bot.ThreadProvider;
import bot.rocketchat.rest.RestClient;
import bot.rocketchat.rest.entities.Room;
import bot.rocketchat.rest.entities.Subscription;
import bot.rocketchat.rest.requests.MessageSendRequest.Attachment;
import bot.rocketchat.rest.responses.ChatCountersResponse;
import bot.rocketchat.rest.responses.GenericHistoryResponse.HistoryMessage;
import bot.rocketchat.rest.responses.MessageSendResponse;
import bot.rocketchat.tasks.LoginTask;
import bot.rocketchat.tasks.RoomTrackerListener;
import bot.rocketchat.tasks.RoomTrackerTask;
import bot.rocketchat.util.ObjectHolder;
import bot.rocketchat.util.Tuple;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.WebsocketClientListener;
import bot.rocketchat.websocket.messages.Base;
import bot.rocketchat.websocket.messages.WebsocketMessageProvider;
import bot.rocketchat.websocket.messages.in.RecChangedStreamRoomMessages;
import bot.rocketchat.websocket.messages.in.RecChangedSub;
import bot.rocketchat.websocket.messages.in.RecConnected;
import bot.rocketchat.websocket.messages.in.RecLogin;
import bot.rocketchat.websocket.messages.in.RecLogin.Result;
import bot.rocketchat.websocket.messages.in.RecWithId;
import bot.rocketchat.websocket.messages.out.SendConnect;
import bot.rocketchat.websocket.messages.out.SendJoinRoom;
import bot.rocketchat.websocket.messages.out.SendPong;
import bot.rocketchat.websocket.messages.out.SendStreamRoomMessages;

public class RocketChatClient extends CommonBase implements WebsocketClientListener, RoomTrackerListener {

	private static final long LOGIN_THREAD_SLEEP_TIME_MILLIS = 60000L;
	private static final long ROOM_TRACKER_THREAD_SLEEP_TIME_MILLIS = 2500L;

	private static final Logger logger = LoggerFactory.getLogger(RocketChatClient.class);

	@Inject
	private Gson json;

	@Inject
	private ConnectionInfo conInfo;
	private RocketChatClientListener listener;

	@Inject
	private WebsocketClient wsClient;
	@Inject
	private RestClient rsClient;

	private final Semaphore syncWaitForConnected = new Semaphore(0);
	private final Semaphore syncWaitForLoggedIn = new Semaphore(0);

	@Inject
	private ThreadProvider threadProvider;

	@Inject
	private LoginTask loginTask;
	@Inject
	private ObjectHolder<RecLogin.Result> loginTokenHolder;
	@Inject
	private RoomTrackerTask roomTrackerTask;

	@Inject
	private WebsocketMessageProvider wsMessages;
	@Inject
	private Provider<Room> roomProvider;
	@Inject
	private Provider<Message> messageProvider;

	private State state = State.DISCONNECTED;

	RocketChatClient() {
	}

	public void setListener(RocketChatClientListener listener) {
		this.listener = listener;
	}

	public void start() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		logger.debug("Starting RocketChatClient...");

		state = State.CONNECTED;
		wsClient.initialize(this);
		rsClient.initialize(loginTokenHolder);

		logger.trace("Opened to WebSocket, initialized REST client.");

		SendConnect sendConnectOut = wsMessages.get(SendConnect.class).initialize();
		wsClient.sendMessage(sendConnectOut);
		syncWaitForConnected.acquire();
		logger.trace("Connected to real-time API (WebSocket).");

		syncWaitForLoggedIn.drainPermits();
		loginTask.initialize(wsClient, LOGIN_THREAD_SLEEP_TIME_MILLIS);
		threadProvider.create(loginTask, "login-thread").start();
		syncWaitForLoggedIn.acquire();
		logger.trace("Logged in via real-time API (WebSocket).");

		roomTrackerTask.initialize(rsClient, ROOM_TRACKER_THREAD_SLEEP_TIME_MILLIS, this);
		threadProvider.create(roomTrackerTask, "room-tracker-thread").start();
		logger.trace("Started room tracker to periodically join open channels.");

		List<Subscription> subs = rsClient.getSubscriptions();
		logger.trace("Subscribing to {} rooms...", subs.size());
		for (Subscription sub : subs) {
			SendStreamRoomMessages sendStreamRoomMessagesMsg = wsMessages.get(SendStreamRoomMessages.class);
			sendStreamRoomMessagesMsg.initialize(sub.getRoomId());
			wsClient.sendMessage(sendStreamRoomMessagesMsg);
		}

		logger.trace("Checking for unread messages in {} rooms...", subs.size());
		for (Subscription sub : subs)
			processRoom(roomProvider.get().parse(sub));

		logger.debug("RocketChatClient started!");
	}

	public boolean sendMessage(String roomId, String text, Attachment... attachments) {
		MessageSendResponse response = rsClient.sendMessage(roomId, text, attachments);
		return response.isSuccessful();
	}

	public void stop() throws IOException {
		logger.debug("Stopping RocketChatClient...");

		roomTrackerTask.stop();
		loginTask.stop();
		wsClient.close();
		wsClient = null;
		rsClient = null;
		loginTokenHolder.reset();
		state = State.DISCONNECTED;

		logger.debug("RocketChatClient stopped!");
	}

	@Override
	public void onWebsocketClose(boolean initiatedByClient) {
		logger.debug("WebsocketClient closed the session.");

		try {
			stop();
		} catch (IOException e) {
			logger.error("Caught unexpected IOException, shutting down!", e);
		}

		listener.onRocketChatClientClose(initiatedByClient);
	}

	@Override
	public void onWebsocketMessage(String message) {
		Base entity = json.fromJson(message, Base.class);

		if (entity.is(PING)) {
			handleMessagePing();
		} else if (entity.is(CONNECTED)) {
			handleMessageConnected(message);
		} else if (entity.is(RESULT)) {
			handleMessageResult(message);
		} else if (entity.is(UPDATED)) {
			// Do nothing
		} else if (entity.is(ADDED)) {
			// Do nothing
		} else if (entity.is(READY)) {
			// Do nothing, occurs on successful real-time stream subscription
		} else if (entity.is(CHANGED)) {
			handleMessageChanged(message);
		} else {
			logger.debug("Unhandled message received in class '{}': '{}'!", getClass().getSimpleName(), message);
		}
	}

	private void handleMessagePing() {
		SendPong sendPongMsg = wsMessages.get(SendPong.class).initialize();
		wsClient.sendMessage(sendPongMsg);
	}

	private void handleMessageConnected(String message) {
		RecConnected connected = json.fromJson(message, RecConnected.class);
		logger.info("Connected and started session '{}'.", connected.getSession());

		syncWaitForConnected.release();
	}

	private void handleMessageResult(String message) {
		RecWithId recWithId = json.fromJson(message, RecWithId.class);
		String id = recWithId.getId();

		if (id.startsWith(LoginTask.ID_PREFIX)) {
			handleLoginResult(message);
		} else {
			// Handle other "results"
			logger.info("Unhandled: {}!", message);

		}
	}

	private void handleMessageChanged(String message) {
		RecChangedSub changedSub = json.fromJson(message, RecChangedSub.class);

		if (changedSub.getCollection().equals(COLLECTION)) {
			RecChangedStreamRoomMessages stream = json.fromJson(message, RecChangedStreamRoomMessages.class);

			Set<String> roomIds = new HashSet<>();

			for (Tuple<String, String> tuple : stream.getMessages())
				roomIds.add(tuple.getA());

			for (String roomId : roomIds)
				processRoom(roomId);
		}
	}

	private void handleLoginResult(String message) {
		RecLogin login = json.fromJson(message, RecLogin.class);
		Result loginToken = login.getResult();
		loginTokenHolder.set(loginToken);

		long expires = Long.parseLong(loginToken.getTokenExpires().get$date());
		logger.info("Logged in as '{}', token expires at '{}'!", conInfo.getUsername(), Instant.ofEpochMilli(expires));

		syncWaitForLoggedIn.release();
	}

	@Override
	public void onNewRooms(List<Room> newRooms) {
		logger.trace("Joining {} new rooms...", newRooms.size());
		for (Room room : newRooms) {
			SendJoinRoom sendJoinRoomMsg = wsMessages.get(SendJoinRoom.class).initialize(room.getId());
			wsClient.sendMessage(sendJoinRoomMsg);
		}

		logger.trace("Subscribing to {} rooms...", newRooms.size());
		for (Room room : newRooms) {
			SendStreamRoomMessages sendStreamRoomMessagesMsg = wsMessages.get(SendStreamRoomMessages.class)
					.initialize(room.getId());
			wsClient.sendMessage(sendStreamRoomMessagesMsg);
		}

		logger.trace("Checking for unread messages in {} rooms...", newRooms.size());
		for (Room room : newRooms)
			processRoom(room);
	}

	private void processRoom(String roomId) {
		Subscription sub = rsClient.getOneSubscription(roomId);

		// If this bot hasn't entered the room yet, then there's no subscription.
		// Unread messages will be processed later.
		if (sub == null || sub.getRoomId() == null)
			return;

		Room room = roomProvider.get().parse(sub);
		processRoom(room);
	}

	private void processRoom(Room room) {
		logger.trace("Processing room '{}'...", room.getId());
		ChatCountersResponse counters = rsClient.getChatCounters(room);

		List<HistoryMessage> history = rsClient.getChatHistory(room, counters);
		logger.trace("Found '{}' unread messages for room '{}'.", history.size(), room.getId());

		if (!history.isEmpty()) {
			rsClient.markSubscriptionRead(room.getId());

			for (HistoryMessage hm : history) {
				Message m = messageProvider.get();
				m.initialize(hm.getId(), hm.getText(), hm.getRoomId(), hm.getTimeStamp(), hm.getType());
				listener.onRocketChatClientMessage(m);
			}
		}
	}

	public State getState() {
		return state;
	}

	public static enum State {
		DISCONNECTED, CONNECTED
	}
}