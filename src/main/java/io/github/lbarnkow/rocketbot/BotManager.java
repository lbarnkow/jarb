package io.github.lbarnkow.rocketbot;

import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.rocketbot.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.ACQUIRED_LEADERSHIP;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.LOST_LEADERSHIP;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.REALTIME_SESSION_CLOSED;
import static io.github.lbarnkow.rocketbot.misc.EventTypes.REALTIME_SESSION_ESTABLISHED;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.election.ElectionCandidate;
import io.github.lbarnkow.rocketbot.election.ElectionCandidateListener;
import io.github.lbarnkow.rocketbot.election.ElectionCandidateState;
import io.github.lbarnkow.rocketbot.misc.EventTypes;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClientListener;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendLogin;
import io.github.lbarnkow.rocketbot.taskmanager.Task;
import io.github.lbarnkow.rocketbot.taskmanager.TaskManager;

public class BotManager extends Task implements ElectionCandidateListener, RealtimeClientListener {

	private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

	private TaskManager tasks;
	private ElectionCandidate election;
	private RealtimeClient realtimeClient;

	private BotManagerConfiguration config;
	private Bot[] bots;
	private AtomicBoolean shuttingDown = new AtomicBoolean(false);

	private Semaphore eventPool = new Semaphore(0);
	private BlockingDeque<Event> eventQueue = new LinkedBlockingDeque<>();

	@Inject
	BotManager(TaskManager taskManager, ElectionCandidate election, RealtimeClient realtimeClient) {
		this.tasks = taskManager;
		this.election = election;
		this.realtimeClient = realtimeClient;
	}

	public void start(BotManagerConfiguration config, Bot... bots) {
		this.config = config;
		this.bots = bots;

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
			// TODO
		}
	}

	@Override
	protected void initializeTask() throws Throwable {
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
			keepGoing = handleRealtimeSessionEstablishedEvent();

		} else if (event.type == REALTIME_SESSION_CLOSED) {
			keepGoing = handleRealtimeSessionClosed(event);

		}

		return keepGoing;
	}

	private boolean handleLostLeadership() {
		logger.info("Lost leadership lease; shutting down!");
		return false;
	}

	private boolean handleAcquiredLeadershipEvent() throws URISyntaxException, DeploymentException, IOException {
		realtimeClient.connect(this, config.getConnection());
		return true;
	}

	private boolean handleRealtimeSessionEstablishedEvent() {
		logger.info("Real-time session established; logging in bots...");

		for (Bot bot : bots) {
			try {
				SendLogin message = new SendLogin("demobot", "demobot");
				logger.error(message.toString());
				String string = realtimeClient.sendMessageAndWait(message);
				logger.info(string);
			} catch (JsonProcessingException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
		// login all bots (login task per bot)
		// -- if a login fails, shutdown!

		// On each successful login ->
		//// (add real-time subscription to all joined rooms for all bots)
		//// catch up on all channels for all bots
		//// start room-join-task (per bot?)
		//// done starting :)

	}

	private boolean handleRealtimeSessionClosed(Event event) {
		boolean initiatedByClient = (boolean) event.data;
		if (!initiatedByClient) {
			logger.info("Realtime connection was closed by other side; shutting down!");
		}
		return false;
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
	public void onRealtimeClientSessionEstablished() {
		Event event = new Event(REALTIME_SESSION_ESTABLISHED, null);
		enqueueEvent(event);
	}

	@Override
	public void onRealtimeClientSessionClose(boolean initiatedByClient) {
		Event event = new Event(REALTIME_SESSION_CLOSED, initiatedByClient);
		enqueueEvent(event);
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
