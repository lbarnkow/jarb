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

package io.github.lbarnkow.jarb.rocketchat.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.lbarnkow.jarb.rocketchat.ConnectionConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * A simplified abstraction from the <code>javax.websocket</code> API.
 * 
 * @author lbarnkow
 */
@ClientEndpoint
@Slf4j
public class WebsocketClient {
  /**
   * A externally provided web socket container hosting all web socket
   * connections.
   */
  private transient WebSocketContainer container;

  /**
   * A listener to be informed on various events.
   */
  private transient WebsocketClientListener listener;

  /**
   * The web socket session.
   */
  private transient Session session;

  /**
   * A flag indicating if the session was closed by the client.
   */
  private transient boolean closedByClient;

  /**
   * Counts the number of messages received.
   */
  private transient long receivedMessages;

  @Inject
  WebsocketClient(WebSocketContainer container) {
    this.container = container;
  }

  /**
   * Initializes this instance with configuration data and an event listener.
   *
   * @param config   the parsed configuration
   * @param listener a listener to inform about incoming websocket events
   * @throws URISyntaxException  on malformed remote server URLs
   * @throws DeploymentException on connections issues
   * @throws IOException         on io errors
   */
  public void initialize(ConnectionConfiguration config, WebsocketClientListener listener)
      throws URISyntaxException, DeploymentException, IOException {
    this.listener = listener;
    URI endpointUri = new URI(config.getWebsocketUrl());

    log.debug("Opening Websocket connection.");

    session = container.connectToServer(this, endpointUri);
  }

  /**
   * Sends a plain text message to the chat server.
   * 
   * @param message the message
   * @throws JsonProcessingException on serialization errors
   */
  public void sendMessage(String message) throws JsonProcessingException {
    log.debug("Sending message, session id '{}', message '{}'.", session.getId(), message);
    session.getAsyncRemote().sendText(message);
  }

  /**
   * Called by the web socket container upon successfully opening the session.
   * 
   * @param session the session
   */
  @OnOpen
  public void onOpen(Session session) {
    log.debug("Opened Websocket, session id '{}'.", session.getId());
  }

  /**
   * Called by the web socket container upon closing of the session.
   *
   * @param userSession the session being closed
   * @param reason      the reason
   */
  @OnClose
  public void onClose(Session userSession, CloseReason reason) {
    log.debug("Closed Websocket session: code '{}', message '{}'.", reason.getCloseCode().getCode(),
        reason.getReasonPhrase());
    listener.onWebsocketClose(closedByClient);
  }

  /**
   * Called by the web socket container upon receiving a message to offload
   * interpretation of its contents.
   *
   * @param message the received message
   */
  @OnMessage
  public void onMessage(String message) {
    log.trace("Received Websocket message: '{}'.", message);

    // First successful message is '{"server_id":"0"}' - just discard.
    if (receivedMessages == 0) {
      receivedMessages++;
      if (message.contains("error")) {
        log.error("Closing connection due to receiving an error initiating the session: {}",
            message);
        try {
          close();
        } catch (IOException e) {
          log.error("Failed to close session; still informing listener!", e);
          listener.onWebsocketClose(true);
        }
      }
      return;
    }

    listener.onWebsocketMessage(message);
  }

  /**
   * Closes the active web socket session.
   *
   * @throws IOException on io errors
   */
  public void close() throws IOException {
    if (!session.isOpen()) {
      return;
    }

    log.debug("Closing Websocket, session id '{}'.", session.getId());

    closedByClient = true;
    session.close();
  }

}
