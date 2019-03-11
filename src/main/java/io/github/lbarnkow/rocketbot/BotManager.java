package io.github.lbarnkow.rocketbot;

import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.ACQUIRED_LEADERSHIP;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.AUTH_TOKEN_REFRESHED;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.LOST_LEADERSHIP;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.NEW_SUBSCRIPTION;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.REALTIME_SESSION_CLOSED;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.REALTIME_SESSION_ESTABLISHED;

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

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.api.Bot.AuthInfo;
import io.github.lbarnkow.rocketbot.election.ElectionCandidate;
import io.github.lbarnkow.rocketbot.election.ElectionCandidateListener;
import io.github.lbarnkow.rocketbot.election.ElectionCandidateState;
import io.github.lbarnkow.rocketbot.misc.EventTypes;
import io.github.lbarnkow.rocketbot.misc.Triple;
import io.github.lbarnkow.rocketbot.misc.Tuple;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClientListener;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendStreamRoomMessages;
import io.github.lbarnkow.rocketbot.rocketchat.rest.RestClient;
import io.github.lbarnkow.rocketbot.taskmanager.Task;
import io.github.lbarnkow.rocketbot.taskmanager.TaskManager;
import io.github.lbarnkow.rocketbot.tasks.LoginTask;
import io.github.lbarnkow.rocketbot.tasks.LoginTask.LoginTaskListener;
import io.github.lbarnkow.rocketbot.tasks.PublicChannelAutoJoinerTask;
import io.github.lbarnkow.rocketbot.tasks.SubscriptionsTrackerTask;
import io.github.lbarnkow.rocketbot.tasks.SubscriptionsTrackerTask.SubscriptionsTrackerTaskListener;

public class BotManager extends Task implements ElectionCandidateListener, RealtimeClientListener, LoginTaskListener,
		SubscriptionsTrackerTaskListener {

	private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

	private TaskManager tasks;
	private ElectionCandidate election;

	private BotManagerConfiguration config;
	private Provider<RealtimeClient> realtimeClientProvider;
	private Map<Bot, RealtimeClient> realtimeClients = new ConcurrentHashMap<>();
	private RestClient restClient;
	private AtomicBoolean shuttingDown = new AtomicBoolean(false);

	private Semaphore eventPool = new Semaphore(0);
	private BlockingDeque<Event> eventQueue = new LinkedBlockingDeque<>();

	@Inject
	BotManager(TaskManager taskManager, ElectionCandidate election, Provider<RealtimeClient> realtimeClientProvider,
			RestClient restClient) {
		this.tasks = taskManager;
		this.election = election;
		this.realtimeClientProvider = realtimeClientProvider;
		this.restClient = restClient;
	}

	public void start(BotManagerConfiguration config, Bot... bots) {
		this.config = config;

		for (Bot bot : bots) {
			realtimeClients.put(bot, realtimeClientProvider.get());
		}
		restClient.initialize(config.getConnection());

		tasks.start(this);

		election.configure(this, this.config.getElection());
		tasks.start(election);
	}

	public void stop() {
		if (shuttingDown.getAndSet(true) == false) {
			logger.info("Stopping all real-time subscriptions...");
			// TODO

			logger.info("Stopping all background tasks...");
			tasks.stopAll();

			logger.info("Closing websocket session...");
			for (RealtimeClient realtimeClient : realtimeClients.values()) {
				try {
					realtimeClient.disconnect();
				} catch (IOException e) {
					Bot bot = findBotFor(realtimeClient);
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

	private boolean handleEvent(Event event) throws URISyntaxException, DeploymentException, IOException {
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

		}

		return keepGoing;
	}

	private boolean handleLostLeadership() {
		logger.info("Lost leadership lease; shutting down!");
		return false;
	}

	private boolean handleAcquiredLeadershipEvent() throws URISyntaxException, DeploymentException, IOException {
		for (RealtimeClient realtimeClient : realtimeClients.values()) {
			realtimeClient.connect(this, config.getConnection());
		}
		return true;
	}

	private boolean handleRealtimeSessionEstablishedEvent(Event event) {
		Bot bot = (Bot) event.data;
		RealtimeClient realtimeClient = realtimeClients.get(bot);

		logger.info("Real-time session established for bot '{}'; logging in bot...", bot.getName());

		LoginTask loginTask = new LoginTask(bot, realtimeClient, this);
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
		AuthInfo previousAuthInfo = tuple.getSecond();
		AuthInfo authInfo = bot.getAuthHolder().get();

		if (authInfo.isValid() && !previousAuthInfo.isValid()) {
			RealtimeClient realtimeClient = realtimeClients.get(bot);
			tasks.start(new SubscriptionsTrackerTask(bot, realtimeClient, this));
			tasks.start(new PublicChannelAutoJoinerTask(bot, restClient, realtimeClient));
		}

		return true;
	}

	private boolean handleNewSubscription(Event event) throws JsonProcessingException {
		@SuppressWarnings("unchecked")
		Triple<Bot, String, String> triple = (Triple<Bot, String, String>) event.data;
		Bot bot = triple.getFirst();
		String roomId = triple.getSecond();
		String roomName = triple.getThird();

		SendStreamRoomMessages message = new SendStreamRoomMessages(roomId);
		RealtimeClient realtimeClient = realtimeClients.get(bot);
		realtimeClient.sendMessage(message);

		// TODO: catch up on all channels for all bots --> processRoom(…)
		logger.info("Added realtime subscription to room '{}' (id '{}') for bot '{}'.", //
				roomName, roomId, bot.getName());

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
		Bot bot = findBotFor(source);
		Event event = new Event(REALTIME_SESSION_ESTABLISHED, bot);
		enqueueEvent(event);
	}

	@Override
	public void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient) {
		Tuple<Bot, Boolean> tuple = new Tuple<>(findBotFor(source), initiatedByClient);
		Event event = new Event(REALTIME_SESSION_CLOSED, tuple);
		enqueueEvent(event);
	}

	@Override
	public void onRealtimeClientStreamRoomMessagesUpdate(RealtimeClient source, String roomId) {
		Bot bot = findBotFor(source);
		logger.error("Bot '{}' needs to process room '{}'!", bot.getName(), roomId);
		// TODO:
	}

	@Override
	public void onLoginAuthTokenRefreshed(Bot bot, AuthInfo previousAuthInfo) {
		Tuple<Bot, AuthInfo> tuple = new Tuple<>(bot, previousAuthInfo);
		Event event = new Event(AUTH_TOKEN_REFRESHED, tuple);
		enqueueEvent(event);
	}

	@Override
	public void onNewSubscription(SubscriptionsTrackerTask subscriptionsTrackerTask, String roomId, String roomName) {
		Triple<Bot, String, String> triple = new Triple<>(subscriptionsTrackerTask.getBot(), roomId, roomName);
		Event event = new Event(NEW_SUBSCRIPTION, triple);
		enqueueEvent(event);
	}

	private Bot findBotFor(RealtimeClient realtimeClient) {
		for (Map.Entry<Bot, RealtimeClient> entry : realtimeClients.entrySet()) {
			if (entry.getValue() == realtimeClient) {
				return entry.getKey();
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
}
