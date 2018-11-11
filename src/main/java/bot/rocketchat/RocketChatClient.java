package bot.rocketchat;

import static bot.rocketchat.websocket.MessageTypes.ADDED;
import static bot.rocketchat.websocket.MessageTypes.CHANGED;
import static bot.rocketchat.websocket.MessageTypes.CONNECTED;
import static bot.rocketchat.websocket.MessageTypes.PING;
import static bot.rocketchat.websocket.MessageTypes.READY;
import static bot.rocketchat.websocket.MessageTypes.RESULT;
import static bot.rocketchat.websocket.MessageTypes.UPDATED;
import static bot.rocketchat.websocket.messages.RecChangedStreamRoomMessages.COLLECTION;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.CommonBase;
import bot.ConnectionInfo;
import bot.rocketchat.rest.RestClient;
import bot.rocketchat.rest.Room;
import bot.rocketchat.rest.Subscription;
import bot.rocketchat.rest.responses.ChatCountersResponse;
import bot.rocketchat.rest.responses.GenericHistoryResponse.HistoryMessage;
import bot.rocketchat.tasks.LoginTask;
import bot.rocketchat.tasks.RoomTrackerTask;
import bot.rocketchat.tasks.RoomTrackerTask.RoomTrackerListener;
import bot.rocketchat.util.ObjectHolder;
import bot.rocketchat.util.Tuple;
import bot.rocketchat.websocket.WebsocketClient;
import bot.rocketchat.websocket.WebsocketClientListener;
import bot.rocketchat.websocket.messages.Base;
import bot.rocketchat.websocket.messages.RecChangedStreamRoomMessages;
import bot.rocketchat.websocket.messages.RecChangedSub;
import bot.rocketchat.websocket.messages.RecConnected;
import bot.rocketchat.websocket.messages.RecLogin;
import bot.rocketchat.websocket.messages.RecLogin.Result;
import bot.rocketchat.websocket.messages.RecWithId;
import bot.rocketchat.websocket.messages.SendConnect;
import bot.rocketchat.websocket.messages.SendJoinRoom;
import bot.rocketchat.websocket.messages.SendPong;
import bot.rocketchat.websocket.messages.SendStreamRoomMessages;

public class RocketChatClient extends CommonBase implements WebsocketClientListener, RoomTrackerListener {

	private static final long LOGIN_THREAD_SLEEP_TIME_MILLIS = 60000L;
	private static final long ROOM_TRACKER_THREAD_SLEEP_TIME_MILLIS = 2500L;

	private static final Logger logger = LoggerFactory.getLogger(RocketChatClient.class);

	private final Pattern pattern;

	private final ConnectionInfo conInfo;
	private final RocketChatClientListener listener;
	private WebsocketClient wsClient;
	private RestClient rsClient;

	private final Semaphore syncWaitForConnected = new Semaphore(0);
	private final Semaphore syncWaitForLoggedIn = new Semaphore(0);
	private Thread loginThread;
	private ObjectHolder<RecLogin.Result> loginTokenHolder = new ObjectHolder<>();

	private Thread roomTrackerThread;

	private Map<String, Room> knownRooms = new ConcurrentSkipListMap<>();

	private State state = State.DISCONNECTED;

	public RocketChatClient(ConnectionInfo conInfo, RocketChatClientListener listener) {
		String regex = "^(?:(?:BOTNAME\\s.*)|(?:@BOTNAME\\s.*))";
		regex = regex.replaceAll("BOTNAME", "demobot");

		this.pattern = Pattern.compile(regex);

		this.conInfo = conInfo;
		this.listener = listener;
	}

	public void start() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
		state = State.CONNECTED;
		wsClient = new WebsocketClient(conInfo, this);
		rsClient = new RestClient(conInfo, loginTokenHolder);

		wsClient.sendMessage(new SendConnect());
		syncWaitForConnected.acquire();

		syncWaitForLoggedIn.drainPermits();
		loginThread = new Thread(new LoginTask(conInfo, wsClient, LOGIN_THREAD_SLEEP_TIME_MILLIS));
		loginThread.start();
		syncWaitForLoggedIn.acquire();

		roomTrackerThread = new Thread(new RoomTrackerTask(rsClient, ROOM_TRACKER_THREAD_SLEEP_TIME_MILLIS, this));
		roomTrackerThread.start();

		List<Subscription> subs = rsClient.getSubscriptions();
		for (Subscription sub : subs)
			wsClient.sendMessage(new SendStreamRoomMessages(sub.getRoomId()));

		for (Subscription sub : subs)
			processRoom(new Room(sub));
	}

	public void stop() throws IOException {
		loginThread.interrupt();
		wsClient.close();
		wsClient = null;
		rsClient = null;
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
		} else if (READY.matches(entity)) {
			// Do nothing, occurs on successful real-time stream subscription
		} else if (CHANGED.matches(entity)) {
			handleMessageChanged(message);
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

	private void handleMessageChanged(String message) {
		RecChangedSub changedSub = RecChangedSub.parse(message);

		if (changedSub.getCollection().equals(COLLECTION)) {
			RecChangedStreamRoomMessages stream = RecChangedStreamRoomMessages.parse(message);

			Set<String> roomIds = new HashSet<>();

			for (Tuple<String, String> tuple : stream.getMessages())
				roomIds.add(tuple.getA());

			for (String roomId : roomIds)
				processRoom(roomId);
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

	@Override
	public void onNewRooms(List<Room> newRooms) {
		for (Room room : newRooms)
			wsClient.sendMessage(new SendJoinRoom(room.getId()));

		for (Room room : newRooms)
			wsClient.sendMessage(new SendStreamRoomMessages(room.getId()));

		for (Room room : newRooms)
			processRoom(room);
	}

	private void processRoom(String roomId) {
		Room room = null;

		if (knownRooms.containsKey(roomId)) {
			room = knownRooms.get(roomId);
		} else {
			Subscription sub = rsClient.getOneSubscription(roomId);
			room = new Room(sub);
			knownRooms.put(roomId, room);
		}

		processRoom(room);
	}

	private void processRoom(Room room) {
		ChatCountersResponse counters = rsClient.getChatCounters(room, conInfo.getUsername());

		if (counters.getUnreads() > 0) {
			List<HistoryMessage> history = rsClient.getChatHistory(room, counters.getUnreads());
			rsClient.markSubscriptionRead(room.getId());

			for (HistoryMessage hm : history)
				listener.onRocketChatClientMessage(hm.asMessage());
		}
	}

	public State getState() {
		return state;
	}

	public static enum State {
		DISCONNECTED, CONNECTED
	}
}