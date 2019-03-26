package io.github.lbarnkow.jarb.rocketchat.tasks;

import static lombok.AccessLevel.PACKAGE;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.misc.Holder;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveJoinRoomReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendJoinRoom;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;
import io.github.lbarnkow.jarb.taskmanager.AbstractBotSpecificTask;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PublicChannelAutoJoinerTask extends AbstractBotSpecificTask {
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionsTrackerTask.class);

  public static final long DEFAULT_SLEEP_TIME = 1000L * 15L; // 15 seconds

  private final RestClient restClient;
  private final RealtimeClient realtimeClient;
  private final Holder<AuthInfo> authInfo;
  @Getter(PACKAGE)
  private final long sleepTime;

  PublicChannelAutoJoinerTask(RestClient restClient, RealtimeClient realtimeClient, Bot bot,
      Holder<AuthInfo> authInfo, long sleepTime) {
    super(bot);

    this.restClient = restClient;
    this.realtimeClient = realtimeClient;
    this.authInfo = authInfo;
    this.sleepTime = sleepTime;
  }

  public PublicChannelAutoJoinerTask(RestClient restClient, RealtimeClient realtimeClient, Bot bot,
      Holder<AuthInfo> authInfo) {
    this(restClient, realtimeClient, bot, authInfo, DEFAULT_SLEEP_TIME);
  }

  @Override
  public void runTask() throws Throwable {
    Bot bot = getBot();

    try {
      while (true) {
        ChannelListReply channels = restClient.getChannelList(authInfo.getValue());
        ChannelListJoinedReply joinedChannels =
            restClient.getChannelListJoined(authInfo.getValue());

        Map<String, RawChannel> roomIdsNotJoined =
            findChannelsNotJoined(channels.getChannels(), joinedChannels.getChannels());

        for (RawChannel channel : roomIdsNotJoined.values()) {
          Room room = channel.asRoom();

          boolean shouldJoin = bot.offerRoom(room);
          // What about join code?! Currently API doesn't enforce the passwords (0.73)
          if (shouldJoin) {
            SendJoinRoom message = new SendJoinRoom(room);
            ReceiveJoinRoomReply reply =
                realtimeClient.sendMessageAndWait(message, ReceiveJoinRoomReply.class);

            if (reply.isSuccess()) {
              logger.info("Bot '{}' joined channel '{}'.", bot.getName(), room.getName());
            } else {
              logger.error("Bot '{}' failed to join channel '{}'.", bot.getName(), room.getName());
            }
          }
        }

        Thread.sleep(sleepTime);
      }
    } catch (InterruptedException e) {
      logger.trace("{} was interrupted.", getClass().getSimpleName());
    }
  }

  private Map<String, RawChannel> findChannelsNotJoined(List<RawChannel> channels,
      List<RawChannel> joinedChannels) {
    Map<String, RawChannel> channelsNotJoined = new HashMap<>();

    for (RawChannel channel : channels) {
      channelsNotJoined.put(channel.get_id(), channel);
    }
    for (RawChannel channel : joinedChannels) {
      channelsNotJoined.remove(channel.get_id());
    }

    return channelsNotJoined;
  }
}
