package io.github.lbarnkow.rocketbot;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.api.Message;
import io.github.lbarnkow.rocketbot.api.Room;
import io.github.lbarnkow.rocketbot.api.RoomType;
import io.github.lbarnkow.rocketbot.api.User;
import io.github.lbarnkow.rocketbot.misc.ChronologicalMessageComparator;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.RestClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.rocketbot.rocketchat.rest.RestClientException;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.SubscriptionsGetOneReply;

public class RoomProcessor {
	private static final Logger logger = LoggerFactory.getLogger(RoomProcessor.class);
	private static final ChronologicalMessageComparator COMPARATOR = new ChronologicalMessageComparator();

	private Map<String, Room> roomCache = new ConcurrentHashMap<>();

	public void cacheRoomData(String roomId, String roomName, RoomType roomType) {
		Room room = new Room(roomId, roomName, roomType);
		logger.debug("Cached room object: {}", room);
		roomCache.put(roomId, room);
	}

	public void processRoom(RealtimeClient realtimeClient, RestClient restClient, Bot bot, String roomId)
			throws RestClientException, InterruptedException, ReplyErrorException, IOException {
		Room room = resolveRoomType(restClient, bot, roomId);

		ChatCountersReply countersBefore = restClient.getChatCounters(bot, room.getType(), room.getId());
		logger.debug("Bot '{}' needs to process '{}' unread messages in room '{}'.", bot.getName(),
				countersBefore.getUnreads(), room.getName());

		restClient.markSubscriptionRead(bot, roomId);
		ChatCountersReply countersAfter = restClient.getChatCounters(bot, room.getType(), room.getId());

		List<Message> history = getHistory(restClient, bot, room, countersBefore, countersAfter);
		for (Message message : history) {
			try {
				bot.offerMessage(message);
			} catch (Throwable e) {
				logger.error("Bot '{}' failed to process message '{}'!", bot.getName(), message);
			}
		}
	}

	private Room resolveRoomType(RestClient restClient, Bot bot, String roomId) throws RestClientException {
		if (!roomCache.containsKey(roomId)) {
			SubscriptionsGetOneReply subscription = restClient.getOneSubscription(bot, roomId);

			RoomType roomType = RoomType.parse(subscription.getT());
			Room room = new Room(subscription.getRid(), subscription.getName(), roomType);

			roomCache.put(roomId, room);
		}

		return roomCache.get(roomId);
	}

	private List<Message> getHistory(RestClient restClient, Bot bot, Room room, ChatCountersReply before,
			ChatCountersReply after) throws RestClientException {
		List<Message> result = new ArrayList<>();

		Instant oldest = Instant.parse(before.getUnreadsFrom());
		Instant latest = Instant.parse(after.getLatest());

		ChatHistoryReply history = restClient.getChatHistory(bot, room, latest, oldest, true);
		for (ChatHistoryReply.Message rawMsg : history.getMessages()) {
			User user = new User(rawMsg.getU().get_id(), rawMsg.getU().getUsername());
			Message message = new Message(room, user, rawMsg.get_id(), rawMsg.getMsg(), Instant.parse(rawMsg.getTs()));
			result.add(message);
		}

		Collections.sort(result, COMPARATOR);
		return result;
	}
}