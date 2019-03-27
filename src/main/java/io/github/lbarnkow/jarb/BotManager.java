package io.github.lbarnkow.jarb;

import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Provider;
import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
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

@Slf4j
@ToString
public class BotManager extends AbstractBaseTask implements ElectionCandidateListener,
    RealtimeClientListener, LoginTaskListener, SubscriptionsTrackerTaskListener, TaskEndedCallback {

  private TaskManager tasks;
  private ElectionCandidate election;

  private BotManagerConfiguration config;
  private Provider<RealtimeClient> realtimeClientProvider;
  private Map<Bot, BotDataStruct> bots = new ConcurrentHashMap<>();
  private RestClient restClient;

  private AtomicBoolean shuttingDown = new AtomicBoolean(false);

  private Semaphore eventPool = new Semaphore(0);
  private BlockingDeque<QueuedEvent> eventQueue = new LinkedBlockingDeque<>();

  private RoomProcessor roomProcessor;

  @Inject
  BotManager(TaskManager taskManager, ElectionCandidate election,
      Provider<RealtimeClient> realtimeClientProvider, RestClient restClient,
      RoomProcessor roomProcessor) {
    this.tasks = taskManager;
    this.election = election;
    this.realtimeClientProvider = realtimeClientProvider;
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
  public void start(BotManagerConfiguration config, Bot... bots) {
    this.config = config;

    for (Bot bot : bots) {
      BotDataStruct dataStruct = new BotDataStruct();
      dataStruct.realtimeClient = realtimeClientProvider.get();
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
    if (shuttingDown.getAndSet(true) == false) {
      log.info("Stopping all background tasks...");
      tasks.stopAll();

      log.info("Closing websocket session...");

      for (Map.Entry<Bot, BotDataStruct> entry : bots.entrySet()) {
        Bot bot = entry.getKey();
        BotDataStruct data = entry.getValue();

        try {
          data.realtimeClient.disconnect();
        } catch (IOException e) {
          log.error("Caught {} while disconnecting realtimeClient for bot '{}'!",
              e.getClass().getSimpleName(), bot.getName(), e);
        }
      }
    }
  }

  @Override
  public void runTask() throws Throwable {
    boolean keepGoing = true;

    try {
      while (keepGoing) {
        eventPool.acquire();
        QueuedEvent event = eventQueue.pollFirst();

        keepGoing = handleEvent(event);
      }

    } catch (InterruptedException e) {
      if (!shuttingDown.get()) {
        log.info("Caught InterruptedException shutting down...");
      }
    } catch (Exception e) {
      log.error("Unexpected Exception; shutting down!", e);
    }

    stop();
  }

  private boolean handleEvent(QueuedEvent event) throws URISyntaxException, DeploymentException,
      IOException, RestClientException, InterruptedException, ReplyErrorException {
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

  private boolean handleTaskEnded(QueuedEvent event) {
    val taskEndedEvent = (TaskEndedEvent) event.data;
    val task = taskEndedEvent.getTask();
    val state = taskEndedEvent.getState();
    val throwable = taskEndedEvent.getLastError().orElse(null);

    if (throwable != null) {
      log.error("A background task '{}' stopped with an error while in state '{}'; shutting down!",
          task.getName(), state, throwable);
    } else {
      log.info("A background task '{}' stopped w/o an error in state '{}'; shutting down!",
          task.getName(), state);
    }

    return false;
  }

  private boolean handleAcquiredLeadershipEvent()
      throws URISyntaxException, DeploymentException, IOException {
    for (BotDataStruct data : bots.values()) {
      data.realtimeClient.connect(this, config.getConnection());
    }
    return true;
  }

  private boolean handleLostLeadership() {
    log.info("Lost leadership lease; shutting down!");
    return false;
  }

  private boolean handleRealtimeSessionEstablishedEvent(QueuedEvent event) {
    Bot bot = (Bot) event.data;
    BotDataStruct dataStruct = bots.get(bot);
    RealtimeClient realtimeClient = dataStruct.realtimeClient;

    log.info("Real-time session established for bot '{}'; logging in bot...", bot.getName());

    LoginTask loginTask = new LoginTask(bot, realtimeClient, this);
    dataStruct.loginTask = loginTask;

    tasks.start(Optional.of(this), loginTask);

    return true;
  }

  private boolean handleRealtimeSessionClosed(QueuedEvent event) {
    @SuppressWarnings("unchecked")
    Tuple<Bot, Boolean> tuple = (Tuple<Bot, Boolean>) event.data;
    Bot bot = tuple.getFirst();
    boolean initiatedByClient = tuple.getSecond();
    if (!initiatedByClient) {
      log.info("Realtime connection for bot '{}' was closed by other side; shutting down!",
          bot.getName());
    }
    return false;
  }

  private boolean handleAuthTokenRefreshed(QueuedEvent event) throws JsonProcessingException {
    @SuppressWarnings("unchecked")
    Tuple<Bot, AuthInfo> tuple = (Tuple<Bot, AuthInfo>) event.data;

    Bot bot = tuple.getFirst();
    AuthInfo newAuthInfo = tuple.getSecond();

    BotDataStruct dataStruct = bots.get(bot);
    AuthInfo previousAuthInfo = dataStruct.authInfo.getValue();

    if (newAuthInfo.isValid() && !previousAuthInfo.isValid()) {
      dataStruct.authInfo.setValue(newAuthInfo);

      SubscriptionsTrackerTask subscriptionsTrackerTask =
          new SubscriptionsTrackerTask(bot, dataStruct.realtimeClient, this);
      PublicChannelAutoJoinerTask autoJoinerTask = new PublicChannelAutoJoinerTask(restClient,
          dataStruct.realtimeClient, bot, dataStruct.authInfo);

      dataStruct.subscriptionsTrackerTask = subscriptionsTrackerTask;
      dataStruct.autoJoinerTask = autoJoinerTask;

      tasks.start(Optional.of(this), subscriptionsTrackerTask);
      tasks.start(Optional.of(this), autoJoinerTask);
    }

    return true;
  }

  private boolean handleNewSubscription(QueuedEvent event) throws JsonProcessingException {
    @SuppressWarnings("unchecked")
    Tuple<Bot, Room> tuple = (Tuple<Bot, Room>) event.data;
    Bot bot = tuple.getFirst();
    Room room = tuple.getSecond();
    BotDataStruct dataStruct = bots.get(bot);

    // Add realtime subscription
    SendStreamRoomMessages message = new SendStreamRoomMessages(room);
    RealtimeClient realtimeClient = dataStruct.realtimeClient;
    realtimeClient.sendMessage(message);

    log.info("Added real-time subscription for bot '{}' to room '{}'.", bot.getName(),
        room.getName());

    // catch-up on unread messages
    Tuple<Bot, String> newTuple = new Tuple<>(bot, room.getId());
    QueuedEvent newEvent = new QueuedEvent(EventTypes.PROCESS_ROOM, newTuple);
    enqueueEvent(newEvent);

    return true;
  }

  private boolean handleProcessRoom(QueuedEvent event)
      throws RestClientException, InterruptedException, ReplyErrorException, IOException {
    @SuppressWarnings("unchecked")
    Tuple<Bot, String> tuple = (Tuple<Bot, String>) event.data;
    Bot bot = tuple.getFirst();
    String roomId = tuple.getSecond();
    BotDataStruct dataStruct = bots.get(bot);

    roomProcessor.processRoom(dataStruct.realtimeClient, restClient, dataStruct.authInfo.getValue(),
        bot, roomId);

    return true;
  }

  @Override
  public void onTaskEnded(TaskEndedEvent taskEndedEvent) {
    val event = new QueuedEvent(EventTypes.TASK_ENDED, taskEndedEvent);
    enqueueEvent(event);
  }

  @Override
  public void onStateChanged(ElectionCandidate candidate, ElectionCandidateState oldState,
      ElectionCandidateState newState) {
    if (newState == LEADER) {
      QueuedEvent event = new QueuedEvent(EventTypes.ACQUIRED_LEADERSHIP, null);
      enqueueEvent(event);
    } else if (newState == INACTIVE && oldState != null) {
      QueuedEvent event = new QueuedEvent(EventTypes.LOST_LEADERSHIP, null);
      enqueueEvent(event);
    }
  }

  @Override
  public void onRealtimeClientSessionEstablished(RealtimeClient source) {
    Bot bot = lookupBotFor(source);
    QueuedEvent event = new QueuedEvent(EventTypes.REALTIME_SESSION_ESTABLISHED, bot);
    enqueueEvent(event);
  }

  @Override
  public void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient) {
    Tuple<Bot, Boolean> tuple = new Tuple<>(lookupBotFor(source), initiatedByClient);
    QueuedEvent event = new QueuedEvent(EventTypes.REALTIME_SESSION_CLOSED, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onRealtimeClientStreamRoomMessagesUpdate(RealtimeClient source, String roomId) {
    Bot bot = lookupBotFor(source);
    Tuple<Bot, String> tuple = new Tuple<>(bot, roomId);
    QueuedEvent event = new QueuedEvent(EventTypes.PROCESS_ROOM, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onLoginAuthTokenRefreshed(LoginTask source, Bot bot, AuthInfo authInfo) {
    Tuple<Bot, AuthInfo> tuple = new Tuple<>(bot, authInfo);
    QueuedEvent event = new QueuedEvent(EventTypes.AUTH_TOKEN_REFRESHED, tuple);
    enqueueEvent(event);
  }

  @Override
  public void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room) {
    roomProcessor.cacheRoom(room);
    Tuple<Bot, Room> tuple = new Tuple<>(bot, room);
    QueuedEvent event = new QueuedEvent(EventTypes.NEW_SUBSCRIPTION, tuple);
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

  private void enqueueEvent(QueuedEvent event) {
    eventQueue.add(event);
    eventPool.release();
  }

  private static class QueuedEvent {
    private EventTypes type;
    private Object data;

    QueuedEvent(EventTypes type, Object data) {
      this.type = type;
      this.data = data;
    }
  }

  private static enum EventTypes {
    TASK_ENDED, //
    REALTIME_SESSION_CLOSED, //
    ACQUIRED_LEADERSHIP, //
    LOST_LEADERSHIP, //
    REALTIME_SESSION_ESTABLISHED, //
    AUTH_TOKEN_REFRESHED, //
    NEW_SUBSCRIPTION, //
    PROCESS_ROOM
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
