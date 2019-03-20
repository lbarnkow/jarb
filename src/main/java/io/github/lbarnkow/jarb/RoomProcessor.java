package io.github.lbarnkow.jarb;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.MessageType;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.api.User;
import io.github.lbarnkow.jarb.misc.ChronologicalMessageComparator;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsGetOneReply;

public class RoomProcessor {
	private static final Logger logger = LoggerFactory.getLogger(RoomProcessor.class);
	private static final ChronologicalMessageComparator COMPARATOR = new ChronologicalMessageComparator();

	private Map<String, Room> roomCache = new ConcurrentHashMap<>();

	public void cacheRoom(Room room) {
		logger.debug("Cached room object: {}", room);
		roomCache.put(room.getId(), room);
	}

	public void processRoom(RealtimeClient realtimeClient, RestClient restClient, AuthInfo authInfo, Bot bot,
			String roomId) throws RestClientException, InterruptedException, ReplyErrorException, IOException {
		Room room = resolveRoomType(restClient, authInfo, roomId);

		ChatCountersReply countersBefore = restClient.getChatCounters(authInfo, room.getType(), room.getId());
		if (countersBefore.isJoined()) {
			logger.debug("Bot '{}' needs to process (at least) '{}' unread messages in room '{}'.", bot.getName(),
					countersBefore.getUnreads(), room.getName());

			restClient.markSubscriptionRead(authInfo, roomId);
			ChatCountersReply countersAfter = restClient.getChatCounters(authInfo, room.getType(), room.getId());

			List<Message> history = getHistory(restClient, authInfo, room, countersBefore, countersAfter);
			for (Message message : history) {
				try {
					bot.offerMessage(message);
				} catch (Throwable e) {
					logger.error("Bot '{}' failed to process message '{}'!", bot.getName(), message);
				}
			}
		} else {
			logger.debug("Not processing room '{}', because bot '{}' is not a member.", room.getName(), bot.getName());
		}
	}

	private Room resolveRoomType(RestClient restClient, AuthInfo authInfo, String roomId) throws RestClientException {
		if (!roomCache.containsKey(roomId)) {
			SubscriptionsGetOneReply subscription = restClient.getOneSubscription(authInfo, roomId);

			RoomType roomType = RoomType.parse(subscription.getT());
			Room room = new Room(subscription.getRid(), subscription.getName(), roomType);

			roomCache.put(roomId, room);
		}

		return roomCache.get(roomId);
	}

	private List<Message> getHistory(RestClient restClient, AuthInfo authInfo, Room room, ChatCountersReply before,
			ChatCountersReply after) throws RestClientException {
		List<Message> result = new ArrayList<>();

		Instant oldest = Instant.parse(before.getUnreadsFrom());
		Instant latest = Instant.parse(after.getUnreadsFrom());
		ChatHistoryReply history = restClient.getChatHistory(authInfo, room, latest, oldest, true);

		for (ChatHistoryReply.Message rawMsg : history.getMessages()) {
			MessageType type = MessageType.parse(rawMsg.getT());
			User user = new User(rawMsg.getU().get_id(), rawMsg.getU().getUsername());
			Message message = new Message(type, room, user, rawMsg.get_id(), rawMsg.getMsg(),
					Instant.parse(rawMsg.getTs()));
			result.add(message);
		}

		Collections.sort(result, COMPARATOR);
		return result;
	}
}