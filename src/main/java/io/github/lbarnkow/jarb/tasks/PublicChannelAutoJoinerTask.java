package io.github.lbarnkow.jarb.tasks;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.misc.Holder;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendJoinRoom;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PublicChannelAutoJoinerTask extends AbstractBaseTask {
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsTrackerTask.class);

	private static final long DEFAULT_SLEEP_TIME = 1000L * 15L; // 15 seconds

	private final RestClient restClient;
	private final RealtimeClient realtimeClient;
	private final Bot bot;
	private final Holder<AuthInfo> authInfo;
	private final long sleepTime;

	PublicChannelAutoJoinerTask(RestClient restClient, RealtimeClient realtimeClient, Bot bot,
			Holder<AuthInfo> authInfo, long sleepTime) {
		super(bot.getName());

		this.restClient = restClient;
		this.realtimeClient = realtimeClient;
		this.bot = bot;
		this.authInfo = authInfo;
		this.sleepTime = sleepTime;
	}

	public PublicChannelAutoJoinerTask(RestClient restClient, RealtimeClient realtimeClient, Bot bot,
			Holder<AuthInfo> authInfo) {
		this(restClient, realtimeClient, bot, authInfo, DEFAULT_SLEEP_TIME);
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		try {
			while (true) {
				ChannelListReply channels = restClient.getChannelList(authInfo.getValue());
				ChannelListJoinedReply joinedChannels = restClient.getChannelListJoined(authInfo.getValue());

				Map<String, RawChannel> roomIdsNotJoined = new HashMap<>();
				for (RawChannel channel : channels.getChannels()) {
					roomIdsNotJoined.put(channel.get_id(), channel);
				}
				for (RawChannel channel : joinedChannels.getChannels()) {
					roomIdsNotJoined.remove(channel.get_id());
				}

				for (String roomId : roomIdsNotJoined.keySet()) {
					// TODO: offer room to bot!
					// TODO: What about rooms with join code?!
					SendJoinRoom message = new SendJoinRoom(roomId);
					realtimeClient.sendMessage(message);

					String roomName = roomIdsNotJoined.get(roomId).getName();
					logger.info("Bot '{}' joined channel '{}'.", bot.getName(), roomName);
				}

				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException e) {
		}
	}
}
