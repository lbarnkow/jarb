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

package io.github.lbarnkow.jarb.rocketchat;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbarnkow.jarb.misc.Tuple;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.WebsocketClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.WebsocketClientListener;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.BaseMessage;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveStreamRoomMessagesSubscriptionUpdate;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveStreamRoomMessagesSubscriptionUpdate.Arg;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendConnect;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendPong;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendStreamRoomMessages;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.websocket.DeploymentException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("PMD.TooManyMethods")
public class RealtimeClient implements WebsocketClientListener {

  private static final String REC_MSG_CONNECTED = "connected";
  private static final String REC_MSG_PING = "ping";
  private static final String REC_MSG_CHANGED = "changed";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final transient WebsocketClient client;

  private transient RealtimeClientListener listener;

  private final transient Map<String, Object> unansweredRequests = new ConcurrentHashMap<>();

  @Inject
  RealtimeClient(WebsocketClient client) {
    this.client = client;
  }

  /**
   * Establishes a web socket connection to the Rocket.Chat-Server and initiates a
   * realtime session by sending the <i>"connect"</i> message.
   *
   * @param listener a listener to be informed about session state changes and
   *                 incoming messages
   * @param config   the parsed connection configuration
   * @throws URISyntaxException  caused by bad configuration
   * @throws DeploymentException caused by connection issues
   * @throws IOException         on io errors
   */
  public void connect(RealtimeClientListener listener, ConnectionConfiguration config)
      throws URISyntaxException, DeploymentException, IOException {
    this.listener = listener;
    client.initialize(config, this);

    sendMessage(new SendConnect());
  }

  public void disconnect() throws IOException {
    client.close();
  }

  public void sendMessage(Object message) throws JsonProcessingException {
    String json = MAPPER.writeValueAsString(message);
    client.sendMessage(json);
  }

  /**
   * Sends a realtime message and blocks until either a reply with the same
   * <i>id</i> is received or a timeout is reached.
   *
   * @param message   the message to send
   * @param timeout   the timeout in milliseconds
   * @param replyType the expected type of reply
   * @return the reply
   * @throws InterruptedException on thread interruption while waiting for the
   *                              reply
   * @throws ReplyErrorException  on errors received in the JSON reply from the
   *                              server
   * @throws IOException          on io errors
   * @throws TimeoutException     on exceeding the wait timeout
   */
  public <X> X sendMessageAndWait(BaseMessage message, long timeout, Class<X> replyType)
      throws InterruptedException, ReplyErrorException, IOException, TimeoutException {
    if (message.getId() == null) {
      throw new IllegalArgumentException(
          "Messages must have a unique id to be used with 'sendMessageAndWait()'!");
    }

    Semaphore sem = new Semaphore(0);
    unansweredRequests.put(message.getId(), sem);

    sendMessage(message);
    boolean wasAnswered = sem.tryAcquire(timeout, MILLISECONDS);
    Object value = unansweredRequests.remove(message.getId());

    if (!wasAnswered || value == null || sem.equals(value)) {
      throw new TimeoutException(message.toString());
    }

    @SuppressWarnings("unchecked")
    Tuple<BaseMessage, String> reply = (Tuple<BaseMessage, String>) value;
    if (reply.getFirst().getError() != null) {
      throw new ReplyErrorException(reply.getFirst().getError());
    }

    return MAPPER.readValue(reply.getSecond(), replyType);
  }

  public <X> X sendMessageAndWait(BaseMessage message, Class<X> replyType)
      throws InterruptedException, ReplyErrorException, IOException, TimeoutException {
    return sendMessageAndWait(message, 1000L * 60L, replyType);
  }

  @Override
  public void onWebsocketClose(boolean initiatedByClient) {
    listener.onRealtimeClientSessionClose(this, initiatedByClient);
  }

  @Override
  public void onWebsocketMessage(String message) {
    try {
      BaseMessage baseMessage = MAPPER.readValue(message, BaseMessage.class);

      if (REC_MSG_CONNECTED.equals(baseMessage.getMsg())) {
        handleConnected();

      } else if (REC_MSG_PING.equals(baseMessage.getMsg())) {
        handlePing();

      } else if (REC_MSG_CHANGED.equals(baseMessage.getMsg())
          && baseMessage.getCollection() != null) {
        handleSubscriptionUpdate(baseMessage, message);

      } else if (baseMessage.getId() != null) {
        handleMessageWithId(baseMessage, message);

      }

    } catch (IOException e) {
      log.error("Unexpected error deserializing server message '{}'; closing session!", message, e);
      try {
        client.close();
      } catch (IOException e1) {
        log.error("Unexpected error disconnecting!", e1);
      }
    }
  }

  private void handleConnected() {
    listener.onRealtimeClientSessionEstablished(this);
  }

  private void handlePing() throws JsonProcessingException {
    sendMessage(new SendPong());
  }

  private void handleMessageWithId(BaseMessage message, String rawJson) {
    if (unansweredRequests.containsKey(message.getId())) {
      Semaphore semaphore = (Semaphore) unansweredRequests.remove(message.getId());
      unansweredRequests.put(message.getId(), new Tuple<>(message, rawJson));
      semaphore.release();
    } else {
      log.debug("Unhandled message with id: {}", rawJson);
    }
  }

  private void handleSubscriptionUpdate(BaseMessage message, String rawJson)
      throws JsonParseException, JsonMappingException, IOException {
    if (SendStreamRoomMessages.COLLECTION.equals(message.getCollection())) {
      ReceiveStreamRoomMessagesSubscriptionUpdate subscriptionUpdate =
          MAPPER.readValue(rawJson, ReceiveStreamRoomMessagesSubscriptionUpdate.class);
      List<Arg> args = subscriptionUpdate.getFields().getArgs();
      for (Arg arg : args) {
        listener.onRealtimeClientStreamRoomMessagesUpdate(this, arg.getRid());
      }

    } else {
      log.debug("Unhandled subscription update for collection '{}'.", message.getCollection());

    }
  }
}
