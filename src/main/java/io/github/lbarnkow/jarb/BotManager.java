package io.github.lbarnkow.jarb;

import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.jarb.misc.EventTypes.ACQUIRED_LEADERSHIP;
import static io.github.lbarnkow.jarb.misc.EventTypes.AUTH_TOKEN_REFRESHED;
import static io.github.lbarnkow.jarb.misc.EventTypes.LOST_LEADERSHIP;
import static io.github.lbarnkow.jarb.misc.EventTypes.NEW_SUBSCRIPTION;
import static io.github.lbarnkow.jarb.misc.EventTypes.PROCESS_ROOM;
import static io.github.lbarnkow.jarb.misc.EventTypes.REALTIME_SESSION_CLOSED;
import static io.github.lbarnkow.jarb.misc.EventTypes.REALTIME_SESSION_ESTABLISHED;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Provider;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.election.ElectionCandidate;
import io.github.lbarnkow.jarb.election.ElectionCandidateListener;
import io.github.lbarnkow.jarb.election.ElectionCandidateState;
import io.github.lbarnkow.jarb.misc.EventTypes;
import io.github.lbarnkow.jarb.misc.Holder;
import io.github.lbarnkow.jarb.misc.Tuple;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClientListener;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendStreamRoomMessages;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.tasks.LoginTask;
import io.github.lbarnkow.jarb.tasks.PublicChannelAutoJoinerTask;
import io.github.lbarnkow.jarb.tasks.SubscriptionsTrackerTask;
import io.github.lbarnkow.jarb.tasks.LoginTask.LoginTaskListener;
import io.github.lbarnkow.jarb.tasks.SubscriptionsTrackerTask.SubscriptionsTrackerTaskListener;

public class BotManager extends Task implements ElectionCandidateListener, RealtimeClientListener, LoginTaskListener,
		SubscriptionsTrackerTaskListener {

	private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

	private TaskManager tasks;
	private ElectionCandidate election;

	private BotManagerConfiguration config;
	private Provider<RealtimeClient> realtimeClientProvider;
	private Map<Bot, BotDataStruct> bots = new ConcurrentHashMap<>();
	private RestClient restClient;

	private AtomicBoolean shuttingDown = new AtomicBoolean(false);

	private Semaphore eventPool = new Semaphore(0);
	private BlockingDeque<Event> eventQueue = new LinkedBlockingDeque<>();

	private RoomProcessor roomProcessor;

	@Inject
	BotManager(TaskManager taskManager, ElectionCandidate election, Provider<RealtimeClient> realtimeClientProvider,
			RestClient restClient, RoomProcessor roomProcessor) {
		this.tasks = taskManager;
		this.election = election;
		this.realtimeClientProvider = realtimeClientProvider;
		this.restClient = restClient;
		this.roomProcessor = roomProcessor;
	}

	public void start(BotManagerConfiguration config, Bot... bots) {
		this.config = config;

		for (Bot bot : bots) {
			BotDataStruct dataStruct = new BotDataStruct();
			dataStruct.realtimeClient = realtimeClientProvider.get();
			this.bots.put(bot, dataStruct);
		}
		restClient.initialize(config.getConnection());

		tasks.start(this);

		election.configure(this, this.config.getElection());
		tasks.start(election);
	}

	public void stop() {
		if (shuttingDown.getAndSet(true) == false) {
			logger.info("Stopping all background tasks...");
			tasks.stopAll();

			logger.info("Closing websocket session...");

			for (Map.Entry<Bot, BotDataStruct> entry : bots.entrySet()) {
				Bot bot = entry.getKey();
				BotDataStruct data = entry.getValue();

				try {
					data.realtimeClient.disconnect();
				} catch (IOException e) {
					logger.error("Caught {} while disconnecting realtimeClient for bot '{}'!",
							e.getClass().getSimpleName(), bot.getName(), e);
				}
			}
		}
	}

	@Override
	protected void runTask() throws Throwable {
		boolean keepGoing = true;

		try {
			while (keepGoing) {
				eventPool.acquire();
				Event event = eventQueue.pollFirst();

				keepGoing = handleEvent(event);
			}

		} catch (InterruptedException e) {
			if (!shuttingDown.get()) {
				logger.info("Caught InterruptedException shutting down...");
			}
		} catch (Exception e) {
			logger.error("Unexpected Exception; shutting down!", e);
		}

		stop();
	}

	private boolean handleEvent(Event event) throws URISyntaxException, DeploymentException, IOException,
			RestClientException, InterruptedException, ReplyErrorException {
		boolean keepGoing = true;

		if (event.type == ACQUIRED_LEADERSHIP) {
			keepGoing = handleAcquiredLeadershipEvent();

		} else if (event.type == LOST_LEADERSHIP) {
			keepGoing = handleLostLeadership();

		} else if (event.type == REALTIME_SESSION_ESTABLISHED) {
			keepGoing = handleRealtimeSessionEstablishedEvent(event);

		} else if (event.type == REALTIME_SESSION_CLOSED) {
			keepGoing = handleRealtimeSessionClosed(event);

		} else if (event.type == AUTH_TOKEN_REFRESHED) {
			keepGoing = handleAuthTokenRefreshed(event);

		} else if (event.type == NEW_SUBSCRIPTION) {
			keepGoing = handleNewSubscription(event);

		} else if (event.type == EventTypes.PROCESS_ROOM) {
			keepGoing = handleProcessRoom(event);

		}

		return keepGoing;
	}

	private boolean handleLostLeadership() {
		logger.info("Lost leadership lease; shutting down!");
		return false;
	}

	private boolean handleAcquiredLeadershipEvent() throws URISyntaxException, DeploymentException, IOException {
		for (BotDataStruct data : bots.values()) {
			data.realtimeClient.connect(this, config.getConnection());
		}
		return true;
	}

	private boolean handleRealtimeSessionEstablishedEvent(Event event) {
		Bot bot = (Bot) event.data;
		BotDataStruct dataStruct = bots.get(bot);
		RealtimeClient realtimeClient = dataStruct.realtimeClient;

		logger.info("Real-time session established for bot '{}'; logging in bot...", bot.getName());

		LoginTask loginTask = new LoginTask(bot, realtimeClient, this);
		dataStruct.loginTask = loginTask;
		tasks.start(loginTask);

		return true;
	}

	private boolean handleRealtimeSessionClosed(Event event) {
		@SuppressWarnings("unchecked")
		Tuple<Bot, Boolean> tuple = (Tuple<Bot, Boolean>) event.data;
		Bot bot = tuple.getFirst();
		boolean initiatedByClient = tuple.getSecond();
		if (!initiatedByClient) {
			logger.info("Realtime connection for bot '{}' was closed by other side; shutting down!", bot.getName());
		}
		return false;
	}

	private boolean handleAuthTokenRefreshed(Event event) throws JsonProcessingException {
		@SuppressWarnings("unchecked")
		Tuple<Bot, AuthInfo> tuple = (Tuple<Bot, AuthInfo>) event.data;

		Bot bot = tuple.getFirst();
		AuthInfo newAuthInfo = tuple.getSecond();

		BotDataStruct dataStruct = bots.get(bot);
		AuthInfo previousAuthInfo = dataStruct.authInfo.get();

		if (newAuthInfo.isValid() && !previousAuthInfo.isValid()) {
			dataStruct.authInfo.set(newAuthInfo);

			SubscriptionsTrackerTask subscriptionsTrackerTask = new SubscriptionsTrackerTask(bot,
					dataStruct.realtimeClient, this);
			PublicChannelAutoJoinerTask autoJoinerTask = new PublicChannelAutoJoinerTask(restClient,
					dataStruct.realtimeClient, bot, dataStruct.authInfo);

			dataStruct.subscriptionsTrackerTask = subscriptionsTrackerTask;
			dataStruct.autoJoinerTask = autoJoinerTask;

			tasks.start(subscriptionsTrackerTask);
			tasks.start(autoJoinerTask);
		}

		return true;
	}

	private boolean handleNewSubscription(Event event) throws JsonProcessingException {
		@SuppressWarnings("unchecked")
		Tuple<Bot, Room> tuple = (Tuple<Bot, Room>) event.data;
		Bot bot = tuple.getFirst();
		Room room = tuple.getSecond();
		BotDataStruct dataStruct = bots.get(bot);

		// Add realtime subscription
		SendStreamRoomMessages message = new SendStreamRoomMessages(room);
		RealtimeClient realtimeClient = dataStruct.realtimeClient;
		realtimeClient.sendMessage(message);

		logger.info("Added real-time subscription for bot '{}' to room '{}'.", bot.getName(), room.getName());

		// catch-up on unread messages
		Tuple<Bot, String> newTuple = new Tuple<>(bot, room.getId());
		Event newEvent = new Event(PROCESS_ROOM, newTuple);
		enqueueEvent(newEvent);

		return true;
	}

	private boolean handleProcessRoom(Event event)
			throws RestClientException, InterruptedException, ReplyErrorException, IOException {
		@SuppressWarnings("unchecked")
		Tuple<Bot, String> tuple = (Tuple<Bot, String>) event.data;
		Bot bot = tuple.getFirst();
		String roomId = tuple.getSecond();
		BotDataStruct dataStruct = bots.get(bot);

		roomProcessor.processRoom(dataStruct.realtimeClient, restClient, dataStruct.authInfo.get(), bot, roomId);

		return true;
	}

	@Override
	public void onStateChanged(ElectionCandidate candidate, ElectionCandidateState oldState,
			ElectionCandidateState newState) {
		if (newState == LEADER) {
			Event event = new Event(ACQUIRED_LEADERSHIP, null);
			enqueueEvent(event);
		} else if (newState == INACTIVE && oldState != null) {
			Event event = new Event(LOST_LEADERSHIP, null);
			enqueueEvent(event);
		}
	}

	@Override
	public void onRealtimeClientSessionEstablished(RealtimeClient source) {
		Bot bot = lookupBotFor(source);
		Event event = new Event(REALTIME_SESSION_ESTABLISHED, bot);
		enqueueEvent(event);
	}

	@Override
	public void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient) {
		Tuple<Bot, Boolean> tuple = new Tuple<>(lookupBotFor(source), initiatedByClient);
		Event event = new Event(REALTIME_SESSION_CLOSED, tuple);
		enqueueEvent(event);
	}

	@Override
	public void onRealtimeClientStreamRoomMessagesUpdate(RealtimeClient source, String roomId) {
		Bot bot = lookupBotFor(source);
		Tuple<Bot, String> tuple = new Tuple<>(bot, roomId);
		Event event = new Event(PROCESS_ROOM, tuple);
		enqueueEvent(event);
	}

	@Override
	public void onLoginAuthTokenRefreshed(LoginTask source, Bot bot, AuthInfo authInfo) {
		Tuple<Bot, AuthInfo> tuple = new Tuple<>(bot, authInfo);
		Event event = new Event(AUTH_TOKEN_REFRESHED, tuple);
		enqueueEvent(event);
	}

	@Override
	public void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room) {
		roomProcessor.cacheRoom(room);
		Tuple<Bot, Room> tuple = new Tuple<>(bot, room);
		Event event = new Event(NEW_SUBSCRIPTION, tuple);
		enqueueEvent(event);
	}

	private Bot lookupBotFor(RealtimeClient realtimeClient) {
		for (Map.Entry<Bot, BotDataStruct> entry : bots.entrySet()) {
			Bot bot = entry.getKey();
			BotDataStruct dataStruct = entry.getValue();

			if (dataStruct.realtimeClient == realtimeClient) {
				return bot;
			}
		}

		throw new IllegalStateException("No bot associated with RealtimeClient!");
	}

	private void enqueueEvent(Event event) {
		eventQueue.add(event);
		eventPool.release();
	}

	private static class Event {
		private EventTypes type;
		private Object data;

		Event(EventTypes type, Object data) {
			this.type = type;
			this.data = data;
		}
	}

	@SuppressWarnings("unused")
	private static class BotDataStruct {
		private RealtimeClient realtimeClient;
		private Holder<AuthInfo> authInfo = new Holder<>(AuthInfo.INVALID);
		private LoginTask loginTask;
		private SubscriptionsTrackerTask subscriptionsTrackerTask;
		private PublicChannelAutoJoinerTask autoJoinerTask;
	}
}
