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

/**
 * A background task periodically querying the chat server to find new
 * server-side subscriptions to chat rooms for a given bot.
 *
 * @author lbarnkow
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class SubscriptionsTrackerTask extends AbstractBotSpecificTask {
  /**
   * The default interval in which a the chat server should be queried for new
   * subscriptions.
   */
  public static final long SLEEP_TIME = 1000L * 15L; // 15 seconds

  /**
   * The <code>Bot</code> for which sever-side subscriptions should be tracked.
   */
  private final transient Bot bot;

  /**
   * Externally supplied and pre-configured <code>RealtimeClient</code> to use to
   * query the chat server.
   */
  private final transient RealtimeClient realtimeClient;

  /**
   * The interval in which a the chat server should be queried for new
   * subscriptions.
   */
  @Getter(PACKAGE)
  private final transient long sleepTime;

  /**
   * The listener to inform about new subscriptions.
   */
  private final transient SubscriptionsTrackerTaskListener listener;

  /**
   * Constructs a new subscriptions tracker for a given <code>Bot</code> using a
   * given <code>RealtimeClient</code>.
   *
   * @param bot            the <code>Bot</code>
   * @param realtimeClient the <code>RealtimeClient</code>
   * @param sleepTime      the interval between each query
   * @param listener       the listener to inform about new subscriptions
   */
  /* default */ SubscriptionsTrackerTask(final Bot bot, final RealtimeClient realtimeClient,
      final long sleepTime, final SubscriptionsTrackerTaskListener listener) {
    super(bot);

    this.bot = bot;
    this.realtimeClient = realtimeClient;
    this.sleepTime = sleepTime;
    this.listener = listener;
  }

  /**
   * Constructs a new subscriptions tracker for a given <code>Bot</code> using a
   * given <code>RealtimeClient</code>.
   *
   * @param bot            the <code>Bot</code>
   * @param realtimeClient the <code>RealtimeClient</code>
   * @param listener       the listener to inform about new subscriptions
   */
  public SubscriptionsTrackerTask(final Bot bot, final RealtimeClient realtimeClient,
      final SubscriptionsTrackerTaskListener listener) {
    this(bot, realtimeClient, SLEEP_TIME, listener);
  }

  @Override
  public void runTask() throws Exception {
    Set<String> knownIds = new HashSet<>();
    try {
      final SendGetSubscriptions message = new SendGetSubscriptions();
      final Set<String> newIds = new HashSet<>();

      while (true) {
        final ReceiveGetSubscriptionsReply reply =
            realtimeClient.sendMessageAndWait(message, ReceiveGetSubscriptionsReply.class);

        for (final RawSubscription sub : reply.getResult()) {
          newIds.add(sub.getId());

          if (!knownIds.contains(sub.getId())) {
            final String roomId = sub.getRid();
            final String roomName = sub.getName();
            final RoomType roomType = RoomType.parse(sub.getType());
            final Room room = Room.builder().id(roomId).name(roomName).type(roomType).build();
            log.debug("Bot '{}' has a subscription to room '{}'.", bot.getName(), room.getName());
            listener.onNewSubscription(this, bot, room);
          }
        }

        knownIds = newIds;
        newIds.clear();
        Thread.sleep(sleepTime);
      }
    } catch (final InterruptedException e) {
      log.trace("{} was interrupted.", getClass().getSimpleName());
    }
  }

  /**
   * A listener that is to be informed whenever a new server-side subscription is
   * detected for the configured <code>Bot</code>.
   *
   * @author lbarnkow
   */
  public interface SubscriptionsTrackerTaskListener {
    /**
     * Called on every new server-side subscription.
     *
     * @param source the <code>SubscriptionsTrackerTask</code> emitting this event
     * @param bot    the <code>Bot</code> associated with the new subscription
     * @param room   the <code>Room</code> being subscribed to
     */
    void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room);
  }
}
