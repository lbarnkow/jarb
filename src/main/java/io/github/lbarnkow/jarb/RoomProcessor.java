package io.github.lbarnkow.jarb;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendSendMessage;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsGetOneReply;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@EqualsAndHashCode
@Slf4j
public class RoomProcessor {
	private static final ChronologicalMessageComparator COMPARATOR = new ChronologicalMessageComparator();

	private Map<String, Room> roomCache = new ConcurrentHashMap<>();

	public void cacheRoom(Room room) {
		log.debug("Cached room object: {}", room);
		roomCache.put(room.getId(), room);
	}

	public void processRoom(RealtimeClient realtimeClient, RestClient restClient, AuthInfo authInfo, Bot bot,
			String roomId) throws RestClientException, InterruptedException, ReplyErrorException, IOException {
		Room room = resolveRoomType(restClient, authInfo, roomId);

		ChatCountersReply countersBefore = restClient.getChatCounters(authInfo, room.getType(), room.getId());
		if (countersBefore.isJoined()) {
			log.debug("Bot '{}' needs to process (at least) '{}' unread messages in room '{}'.", bot.getName(),
					countersBefore.getUnreads(), room.getName());

			restClient.markSubscriptionRead(authInfo, roomId);
			ChatCountersReply countersAfter = restClient.getChatCounters(authInfo, room.getType(), room.getId());

			List<Message> history = getHistory(restClient, authInfo, room, countersBefore, countersAfter);
			for (Message message : history) {
				Optional<Message> reply = Optional.empty();
				try {
					reply = bot.offerMessage(message);
				} catch (Throwable e) {
					log.error("Bot '{}' failed to process message '{}'!", bot.getName(), message, e);
				}
				try {
					if (reply.isPresent()) {
						realtimeClient.sendMessage(new SendSendMessage(reply.get()));
					}
				} catch (Throwable e) {
					log.error("Failed to send Bot '{}'s reply '{}'!", bot.getName(), reply.get(), e);
				}
			}
		} else {
			log.debug("Not processing room '{}', because bot '{}' is not a member.", room.getName(), bot.getName());
		}
	}

	private Room resolveRoomType(RestClient restClient, AuthInfo authInfo, String roomId) throws RestClientException {
		if (!roomCache.containsKey(roomId)) {
			SubscriptionsGetOneReply subscription = restClient.getOneSubscription(authInfo, roomId);

			String roomName = subscription.getName();
			RoomType roomType = RoomType.parse(subscription.getT());
			Room room = Room.builder().id(roomId).name(roomName).type(roomType).build();

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

		for (RawMessage rawMsg : history.getMessages()) {
			String uId = rawMsg.getU().get_id();
			String uName = rawMsg.getU().getUsername();
			User user = User.builder().id(uId).name(uName).build();

			String mId = rawMsg.get_id();
			String mMsg = rawMsg.getMsg();
			Instant mTs = Instant.parse(rawMsg.getTs());
			MessageType mType = MessageType.parse(rawMsg.getT());
			Message message = Message.builder().id(mId).message(mMsg).room(room).timestamp(mTs).type(mType).user(user)
					.build();
			result.add(message);
		}

		Collections.sort(result, COMPARATOR);
		return result;
	}
}