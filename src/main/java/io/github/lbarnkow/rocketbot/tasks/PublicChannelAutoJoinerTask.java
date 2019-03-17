package io.github.lbarnkow.rocketbot.tasks;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.RestClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendJoinRoom;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChannelListReply;

public class PublicChannelAutoJoinerTask extends AbstractBotNamesTask {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsTrackerTask.class);

	private static final long DEFAULT_SLEEP_TIME = 1000L * 15L; // 15 seconds

	private RestClient restClient;
	private RealtimeClient realtimeClient;
	private final long sleepTime;

	PublicChannelAutoJoinerTask(Bot bot, RestClient restClient, RealtimeClient realtimeClient, long sleepTime) {
		super(bot);

		this.restClient = restClient;
		this.realtimeClient = realtimeClient;
		this.sleepTime = sleepTime;
	}

	public PublicChannelAutoJoinerTask(Bot bot, RestClient restClient, RealtimeClient realtimeClient) {
		this(bot, restClient, realtimeClient, DEFAULT_SLEEP_TIME);
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		Bot bot = getBot();

		try {
			while (true) {
				ChannelListReply channels = restClient.getChannelList(bot);
				ChannelListJoinedReply joinedChannels = restClient.getChannelListJoined(bot);

				Map<String, ChannelListReply.Channel> roomIdsNotJoined = new HashMap<>();
				for (ChannelListReply.Channel channel : channels.getChannels()) {
					roomIdsNotJoined.put(channel.get_id(), channel);
				}
				for (ChannelListJoinedReply.Channel channel : joinedChannels.getChannels()) {
					roomIdsNotJoined.remove(channel.get_id());
				}

				for (String roomId : roomIdsNotJoined.keySet()) {
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
