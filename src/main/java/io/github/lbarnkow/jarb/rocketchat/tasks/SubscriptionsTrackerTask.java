package io.github.lbarnkow.jarb.rocketchat.tasks;

import static lombok.AccessLevel.PACKAGE;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendGetSubscriptions;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawSubscription;
import io.github.lbarnkow.jarb.taskmanager.AbstractBotSpecificTask;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class SubscriptionsTrackerTask extends AbstractBotSpecificTask {

  public static final long DEFAULT_SLEEP_TIME = 1000L * 15L; // 15 seconds

  private final Bot bot;
  private final RealtimeClient realtimeClient;
  @Getter(PACKAGE)
  private final long sleepTime;
  private final SubscriptionsTrackerTaskListener listener;

  SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient, long sleepTime,
      SubscriptionsTrackerTaskListener listener) {
    super(bot);

    this.bot = bot;
    this.realtimeClient = realtimeClient;
    this.sleepTime = sleepTime;
    this.listener = listener;
  }

  public SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient,
      SubscriptionsTrackerTaskListener listener) {
    this(bot, realtimeClient, DEFAULT_SLEEP_TIME, listener);
  }

  @Override
  public void runTask() throws Throwable {
    Set<String> knownIds = new HashSet<>();
    try {
      while (true) {
        SendGetSubscriptions message = new SendGetSubscriptions();
        ReceiveGetSubscriptionsReply reply =
            realtimeClient.sendMessageAndWait(message, ReceiveGetSubscriptionsReply.class);

        Set<String> newIds = new HashSet<>();

        for (RawSubscription sub : reply.getResult()) {
          newIds.add(sub.getId());

          if (!knownIds.contains(sub.getId())) {
            String roomId = sub.getRid();
            String roomName = sub.getName();
            RoomType roomType = RoomType.parse(sub.getType());
            Room room = Room.builder().id(roomId).name(roomName).type(roomType).build();
            log.debug("Bot '{}' has a subscription to room '{}'.", bot.getName(), room.getName());
            listener.onNewSubscription(this, bot, room);
          }
        }

        knownIds = newIds;
        Thread.sleep(sleepTime);
      }
    } catch (InterruptedException e) {
      log.trace("{} was interrupted.", getClass().getSimpleName());
    }
  }

  public static interface SubscriptionsTrackerTaskListener {
    void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room);
  }
}
