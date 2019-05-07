/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb;

import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Provider;
import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.BotConfiguration;
import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.election.ElectionCandidate;
import io.github.lbarnkow.jarb.election.ElectionCandidateListener;
import io.github.lbarnkow.jarb.election.ElectionCandidateState;
import io.github.lbarnkow.jarb.misc.Holder;
import io.github.lbarnkow.jarb.misc.Tuple;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClientListener;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendStreamRoomMessages;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.tasks.LoginTask;
import io.github.lbarnkow.jarb.rocketchat.tasks.LoginTask.LoginTaskListener;
import io.github.lbarnkow.jarb.rocketchat.tasks.PublicChannelAutoJoinerTask;
import io.github.lbarnkow.jarb.rocketchat.tasks.SubscriptionsTrackerTask;
import io.github.lbarnkow.jarb.rocketchat.tasks.SubscriptionsTrackerTask.SubscriptionsTrackerTaskListener;
import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import io.github.lbarnkow.jarb.taskmanager.TaskEndedCallback;
import io.github.lbarnkow.jarb.taskmanager.TaskEndedEvent;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.websocket.DeploymentException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is <i>the</i> central management class in jarb. It
 * <code>ElectionCandidate</code> to try and acquire a leading role within a
 * group of processes. Once it has established leadership it will try to
 * establish connections / sessions to the chat server for each individual
 * <code>Bot</code>. It also manages various related background tasks to
 * continually refresh authorization tokens, to discover new unsubscribed
 * channels or to listen to subscribed channels on a per-bot-basis.
 *
 * @author lbarnkow
 */
@Slf4j
@ToString
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class BotManager extends AbstractBaseTask implements ElectionCandidateListener,
    RealtimeClientListener, LoginTaskListener, SubscriptionsTrackerTaskListener, TaskEndedCallback {

  /**
   * Externally supplied <code>TaskManager</code>. Used to manage and schedule
   * background tasks like public channel discovery.
   */
  private final transient TaskManager tasks;

  /**
   * Externally supplied <code>ElectionCandidate</code>. Used to synchronize with
   * other concurrent jarb instances. This instance will only run bots, when it
   * acquired the leadership position.
   */
  private final transient ElectionCandidate election;

  /**
   * Externally supplied configuration.
   */
  private transient BotManagerConfiguration config;

  /**
   * Externally supplied factory for <code>RealtimeClient</code> instances. Each
   * managed bot will get its own real-time client.
   */
  private final transient Provider<RealtimeClient> rtClientProvider;

  /**
   * Map associating each managed bot with additional data and object references.
   */
  private final transient Map<Bot, BotDataStruct> bots = new ConcurrentHashMap<>();

  /**
   * Externally supplied <code>RestClient</code>, which is shared among all bots.
   */
  private final transient RestClient restClient;

  /**
   * Flag indicating if this instance is shutting down. Used for synchronization
   * purposes, to prevent triggering multiple shutdowns based on different causes
   * from different threads.
   */
  private final transient AtomicBoolean shuttingDown = new AtomicBoolean(false);

  /**
   * Used as signalling device for the main event loop. It will sleep/block until
   * this semaphore indicates a new event is available.
   */
  private final transient Semaphore eventPool = new Semaphore(0);

  /**
   * The main event queue.
   */
  private final transient BlockingDeque<QueuedEvent> eventQueue = new LinkedBlockingDeque<>();

  /**
   * Externally supplied <code>RoomProcessor</code> used to act upon new messages
   * to a <code>Room</code> for a <code>Bot</code>.
   */
  private final transient RoomProcessor roomProcessor;

  @Inject
  /* package */ BotManager(final TaskManager taskManager, final ElectionCandidate election,
      final Provider<RealtimeClient> rtClientProvider, final RestClient restClient,
      final RoomProcessor roomProcessor) {
    super();
    this.tasks = taskManager;
    this.election = election;
    this.rtClientProvider = rtClientProvider;
    this.restClient = restClient;
    this.roomProcessor = roomProcessor;
  }

  /**
   * Starts the main bot managements process. <br>
   * <br>
   * This will spawn a couple of threads for managements tasks and initiate
   * connections to the chat servers.
   *
   * @param config the parsed configuration
   * @param bots   the bots being managed by this instance
   */
  public void start(final BotManagerConfiguration config, final Bot... bots) {
    this.config = config;

    for (final Bot bot : bots) {
      @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // unique objects needed
      final BotDataStruct dataStruct = new BotDataStruct();
      dataStruct.realtimeClient = rtClientProvider.get();
      this.bots.put(bot, dataStruct);
    }
    restClient.initialize(config.getConnection());

    tasks.start(Optional.of(this), this);

    election.configure(this, this.config.getElection());
    tasks.start(Optional.of(this), election);
  }

  /**
   * Stops the main bot management process.<br>
   * <br>
   * This will stop all threads spawned by this instance.
   */
  public void stop() {
    if (!shuttingDown.getAndSet(true)) {
      log.info("Stopping all background tasks...");
      tasks.stopAll();

      log.info("Closing websocket session...");

      for (final Map.Entry<Bot, BotDataStruct> entry : bots.entrySet()) {
        final Bot bot = entry.getKey();
        final BotDataStruct data = entry.getValue();

        try {
          data.realtimeClient.disconnect();
        } catch (final IOException e) {
          log.error("Caught {} while disconnecting realtimeClient for bot '{}'!",
              e.getClass().getSimpleName(), bot.getName(), e);
        }
      }
    }
  }

  @Override
  public void runTask() throws Exception {
    boolean keepGoing = true;

    try {
      while (keepGoing) {
        eventPool.acquire();
        final QueuedEvent event = eventQueue.pollFirst();

        keepGoing = handleEvent(event);
      }

    } catch (final InterruptedException e) {
      if (!shuttingDown.get()) {
        log.info("Caught InterruptedException shutting down...");
      }
    } catch (final Exception e) {
      log.error("Unexpected Exception; shutting down!", e);
    }

    stop();
  }

  private boolean handleEvent(final QueuedEvent event)
      throws URISyntaxException, DeploymentException, IOException, RestClientException,
      InterruptedException, ReplyErrorException {
    boolean keepGoing = true;

    if (event.type == EventTypes.TASK_ENDED) {
      keepGoing = handleTaskEnded(event);

    } else if (event.type == EventTypes.ACQUIRED_LEADERSHIP) {
      keepGoing = handleAcquiredLeadershipEvent();

    } else if (event.type == EventTypes.LOST_LEADERSHIP) {
      keepGoing = handleLostLeadership();

    } else if (event.type == EventTypes.REALTIME_SESSION_ESTABLISHED) {
      keepGoing = handleRealtimeSessionEstablishedEvent(event);

    } else if (event.type == EventTypes.REALTIME_SESSION_CLOSED) {
      keepGoing = handleRealtimeSessionClosed(event);

    } else if (event.type == EventTypes.AUTH_TOKEN_REFRESHED) {
      keepGoing = handleAuthTokenRefreshed(event);

    } else if (event.type == EventTypes.NEW_SUBSCRIPTION) {
      keepGoing = handleNewSubscription(event);

    } else if (event.type == EventTypes.PROCESS_ROOM) {
      keepGoing = handleProcessRoom(event);

    }

    return keepGoing;
  }

  private boolean handleTaskEnded(final QueuedEvent event) {
    final val taskEndedEvent = (TaskEndedEvent) event.data;
    final val task = taskEndedEvent.getTask();
    final val state = taskEndedEvent.getState();
    final val throwable = taskEndedEvent.getLastError().orElse(null);

    if (throwable == null) {
      log.info("A background task '{}' stopped w/o an error in state '{}'; shutting down!",
          task.getName(), state);
    } else {
      log.error("A background task '{}' stopped with an error while in state '{}'; shutting down!",
          task.getName(), state, throwable);
    }

    return false;
  }

  private boolean handleAcquiredLeadershipEvent()
      throws URISyntaxException, DeploymentException, IOException {
    for (final BotDataStruct data : bots.values()) {
      data.realtimeClient.connect(this, config.getConnection());
    }
    return true;
  }

  private boolean handleLostLeadership() {
    log.info("Lost leadership lease; shutting down!");
    return false;
  }

  private boolean handleRealtimeSessionEstablishedEvent(final QueuedEvent event) {
    final Bot bot = (Bot) event.data;
    final BotDataStruct dataStruct = bots.get(bot);
    final RealtimeClient realtimeClient = dataStruct.realtimeClient;

    log.info("Real-time session established for bot '{}'; logging in bot...", bot.getName());

    final Credentials credentials = findCredentials(bot);
    final LoginTask loginTask = new LoginTask(bot, credentials, realtimeClient, this);
    dataStruct.loginTask = loginTask;

    tasks.start(Optional.of(this), loginTask);

    return true;
  }

  private Credentials findCredentials(final Bot bot) {
    for (final BotConfiguration configBot : config.getBots()) {
      if (bot.getName().equals(configBot.getName())) {
        return configBot.getCredentials();
      }
    }
    throw new IllegalStateException("Can't find BotConfiguration for Bot '" + bot.getName() + "'!");
  }

  private boolean handleRealtimeSessionClosed(final QueuedEvent event) {
    @SuppressWarnings("unchecked")
    final Tuple<Bot, Boolean> tuple = (Tuple<Bot, Boolean>) event.data;
    final Bot bot = tuple.getFirst();
    final boolean initiatedByClient = tuple.getSecond();
    if (!initiatedByClient) {
      log.info("Realtime connection for bot '{}' was closed by other side; shutting down!",
          bot.getName());
    }
    return false;
  }

  private boolean handleAuthTokenRefreshed(final QueuedEvent event) throws JsonProcessingException {
    @SuppressWarnings("unchecked")
    final Tuple<Bot, AuthInfo> tuple = (Tuple<Bot, AuthInfo>) event.data;

    final Bot bot = tuple.getFirst();
    final AuthInfo newAuthInfo = tuple.getSecond();

    final BotDataStruct dataStruct = bots.get(bot);
    final AuthInfo previousAuthInfo = dataStruct.authInfo.getValue();

    if (newAuthInfo.isValid() && !previousAuthInfo.isValid()) {
      dataStruct.authInfo.setValue(newAuthInfo);

      final SubscriptionsTrackerTask subsTrackerTask =
          new SubscriptionsTrackerTask(bot, dataStruct.realtimeClient, this);
      final PublicChannelAutoJoinerTask autoJoinerTask = new PublicChannelAutoJoinerTask(restClient,
          dataStruct.realtimeClient, bot, dataStruct.authInfo);

      dataStruct.subsTrackerTask = subsTrackerTask;
      dataStruct.autoJoinerTask = autoJoinerTask;

      tasks.start(Optional.of(this), subsTrackerTask);
      tasks.start(Optional.of(this), autoJoinerTask);
    }

    return true;
  }

  private boolean handleNewSubscription(final QueuedEvent event) throws JsonProcessingException {
    @SuppressWarnings("unchecked")
    final Tuple<Bot, Room> tuple = (Tuple<Bot, Room>) event.data;
    final Bot bot = tuple.getFirst();
    final Room room = tuple.getSecond();
    final BotDataStruct dataStruct = bots.get(bot);

    // Add realtime subscription
    final SendStreamRoomMessages message = new SendStreamRoomMessages(room);
    final RealtimeClient realtimeClient = dataStruct.realtimeClient;
    realtimeClient.sendMessage(message);

    log.info("Added real-time subscription for bot '{}' to room '{}'.", bot.getName(),
        room.getName());

    // catch-up on unread messages
    final Tuple<Bot, String> newTuple = new Tuple<>(bot, room.getId());
    final QueuedEvent newEvent = new QueuedEvent(EventTypes.PROCESS_ROOM, newTuple);
    enqueueEvent(newEvent);

    return true;
  }

  private boolean handleProcessRoom(final QueuedEvent event)
      throws RestClientException, InterruptedException, ReplyErrorException, IOException {
    @SuppressWarnings("unchecked")
    final Tuple<Bot, String> tuple = (Tuple<Bot, String>) event.data;
    final Bot bot = tuple.getFirst();
    final String roomId = tuple.getSecond();
    final BotDataStruct dataStruct = bots.get(bot);

    roomProcessor.processRoom(dataStruct.realtimeClient, restClient, dataStruct.authInfo.getValue(),
        bot, roomId);

    return true;
  }

  @Override
  public void onTaskEnded(final TaskEndedEvent taskEndedEvent) {
    final val event = new QueuedEvent(EventTypes.TASK_ENDED, taskEndedEvent);
    enqueueEvent(event);
  }

  @Override
  public void onStateChanged(final ElectionCandidate candidate,
      final ElectionCandidateState oldState, final ElectionCandidateState newState) {
    if (newState == LEADER) {
      final QueuedEvent event = new QueuedEvent(EventTypes.ACQUIRED_LEADERSHIP, null);
      enqueueEvent(event);
    } else if (newState == INACTIVE && oldState != null) {
      final QueuedEvent event = new QueuedEvent(EventTypes.LOST_LEADERSHIP, null);
      enqueueEvent(event);
    }
  }

  @Override
  public void onRealtimeClientSessionEstablished(final RealtimeClient source) {
    final Bot bot = lookupBotFor(source);
    final QueuedEvent event = new QueuedEvent(EventTypes.REALTIME_SESSION_ESTABLISHED, bot);
    enqueueEvent(event);
  }

  @Override
  public void onRealtimeClientSessionClose(final RealtimeClient source,
      final boolean initiatedByClient) {
    final Tuple<Bot, Boolean> tuple = new Tuple<>(lookupBotFor(source), initiatedByClient);
    final QueuedEvent event = new QueuedEvent(EventTypes.REALTIME_SESSION_CLOSED, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onRealtimeClientStreamRoomMessagesUpdate(final RealtimeClient source,
      final String roomId) {
    final Bot bot = lookupBotFor(source);
    final Tuple<Bot, String> tuple = new Tuple<>(bot, roomId);
    final QueuedEvent event = new QueuedEvent(EventTypes.PROCESS_ROOM, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onLoginAuthTokenRefreshed(final LoginTask source, final Bot bot,
      final AuthInfo authInfo) {
    final Tuple<Bot, AuthInfo> tuple = new Tuple<>(bot, authInfo);
    final QueuedEvent event = new QueuedEvent(EventTypes.AUTH_TOKEN_REFRESHED, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onNewSubscription(final SubscriptionsTrackerTask source, final Bot bot,
      final Room room) {
    roomProcessor.cacheRoom(room);
    final Tuple<Bot, Room> tuple = new Tuple<>(bot, room);
    final QueuedEvent event = new QueuedEvent(EventTypes.NEW_SUBSCRIPTION, tuple);
    enqueueEvent(event);
  }

  private Bot lookupBotFor(final RealtimeClient realtimeClient) {
    for (final Map.Entry<Bot, BotDataStruct> entry : bots.entrySet()) {
      final Bot bot = entry.getKey();
      final BotDataStruct dataStruct = entry.getValue();

      if (dataStruct.realtimeClient == realtimeClient) {
        return bot;
      }
    }

    throw new IllegalStateException("No bot associated with RealtimeClient!");
  }

  private void enqueueEvent(final QueuedEvent event) {
    eventQueue.add(event);
    eventPool.release();
  }

  /**
   * Event data for the main event loop.
   *
   * @author lbarnkow
   */
  private static class QueuedEvent {
    /**
     * The event type.
     */
    private final transient EventTypes type;

    /**
     * The event data (depends on the type).
     */
    private final transient Object data;

    /* package */ QueuedEvent(final EventTypes type, final Object data) {
      this.type = type;
      this.data = data;
    }
  }

  /**
   * The types of events for the main event loop.
   *
   * @author lbarnkow
   */
  private enum EventTypes {
    TASK_ENDED, //
    REALTIME_SESSION_CLOSED, //
    ACQUIRED_LEADERSHIP, //
    LOST_LEADERSHIP, //
    REALTIME_SESSION_ESTABLISHED, //
    AUTH_TOKEN_REFRESHED, //
    NEW_SUBSCRIPTION, //
    PROCESS_ROOM
  }

  /**
   * A simple POJO tying together various object references logically connected to
   * a <code>Bot</code> instance.
   *
   * @author lbarnkow
   */
  @SuppressWarnings("unused")
  private static class BotDataStruct {
    /**
     * The <code>RealtimeClient</code> for this <code>Bot</code>.
     */
    private transient RealtimeClient realtimeClient;

    /**
     * The <code>AuthInfo</code> for this <code>Bot</code>.
     */
    private final transient Holder<AuthInfo> authInfo = new Holder<>(AuthInfo.EXPIRED);

    /**
     * The <code>LoginTask</code> for this <code>Bot</code>.
     */
    private transient LoginTask loginTask;

    /**
     * The <code>SubscriptionsTrackerTask</code> for this <code>Bot</code>.
     */
    private transient SubscriptionsTrackerTask subsTrackerTask;

    /**
     * The <code>PublicChannelAutoJoinerTask</code> for this <code>Bot</code>.
     */
    private transient PublicChannelAutoJoinerTask autoJoinerTask;
  }
}
