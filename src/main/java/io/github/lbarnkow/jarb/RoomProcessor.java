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
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * This class fetches all unread messages for a given <code>Bot</code> and room
 * id and marks the room as read on the chat server.
 *
 * @author lbarnkow
 */
@ToString
@EqualsAndHashCode
@Slf4j
public class RoomProcessor {
  private static final ChronologicalMessageComparator COMPARATOR =
      ChronologicalMessageComparator.CHRONOLOGICAL_MESSAGE_COMPARATOR;

  private Map<String, Room> roomCache = new ConcurrentHashMap<>();

  public void cacheRoom(Room room) {
    log.debug("Cached room object: {}", room);
    roomCache.put(room.getId(), room);
  }

  /**
   * Checks for and processes unread messages in a given <code>Room</code> for a
   * given <code>Bot</code>.
   *
   * @param realtimeClient an initialized and connected
   *                       <code>RealtimeClient</code>
   * @param restClient     a <code>RestClient</code>
   * @param authInfo       the auth token to use for REST calls
   * @param bot            the bot handling unread messages
   * @param roomId         the room id to check for unread messages
   * @throws RestClientException  on REST errors
   * @throws InterruptedException on thread interruption while waiting for
   *                              real-time replies
   * @throws ReplyErrorException  on bad real-time replies
   * @throws IOException          on io errors
   */
  public void processRoom(RealtimeClient realtimeClient, RestClient restClient, AuthInfo authInfo,
      Bot bot, String roomId)
      throws RestClientException, InterruptedException, ReplyErrorException, IOException {
    Room room = resolveRoomType(restClient, authInfo, roomId);

    ChatCountersReply countersBefore =
        restClient.getChatCounters(authInfo, room.getType(), room.getId());
    if (countersBefore.isJoined() || room.getType() == RoomType.INSTANT_MESSAGE) {
      log.debug("Bot '{}' needs to process (at least) '{}' unread messages in room '{}'.",
          bot.getName(), countersBefore.getUnreads(), room.getName());

      restClient.markSubscriptionRead(authInfo, roomId);
      ChatCountersReply countersAfter =
          restClient.getChatCounters(authInfo, room.getType(), room.getId());

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
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // don't reuse msg objects
            SendSendMessage outgoing = new SendSendMessage(reply.get());
            realtimeClient.sendMessage(outgoing);
          }
        } catch (Throwable e) {
          log.error("Failed to send Bot '{}'s reply '{}'!", bot.getName(), reply.get(), e);
        }
      }
    } else {
      log.debug("Not processing room '{}', because bot '{}' is not a member.", room.getName(),
          bot.getName());
    }
  }

  private Room resolveRoomType(RestClient restClient, AuthInfo authInfo, String roomId)
      throws RestClientException {
    if (!roomCache.containsKey(roomId)) {
      SubscriptionsGetOneReply subscription = restClient.getOneSubscription(authInfo, roomId);

      String roomName = subscription.getName();
      RoomType roomType = RoomType.parse(subscription.getType());
      Room room = Room.builder().id(roomId).name(roomName).type(roomType).build();

      roomCache.put(roomId, room);
    }

    return roomCache.get(roomId);
  }

  private List<Message> getHistory(RestClient restClient, AuthInfo authInfo, Room room,
      ChatCountersReply before, ChatCountersReply after) throws RestClientException {
    List<Message> result = new ArrayList<>();

    Instant oldest = Instant.parse(before.getUnreadsFrom());
    Instant latest = Instant.parse(after.getUnreadsFrom());
    ChatHistoryReply history = restClient.getChatHistory(authInfo, room, latest, oldest, true);

    for (RawMessage rawMsg : history.getMessages()) {
      String userId = rawMsg.getUser().getId();
      String userName = rawMsg.getUser().getUsername();
      User user = User.builder().id(userId).name(userName).build();

      String rmId = rawMsg.getId();
      String rmMsg = rawMsg.getMsg();
      Instant rmTs = Instant.parse(rawMsg.getTs());
      MessageType rmType = MessageType.parse(rawMsg.getType());
      Message message = Message.builder().id(rmId).message(rmMsg).room(room).timestamp(rmTs)
          .type(rmType).user(user).build();
      result.add(message);
    }

    Collections.sort(result, COMPARATOR);
    return result;
  }
}