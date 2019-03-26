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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class WebsocketClient {
  private static final Logger logger = LoggerFactory.getLogger(WebsocketClient.class);

  private WebSocketContainer container;

  private WebsocketClientListener listener;
  private Session session;

  private boolean closedByClient = false;

  private long receivedMessages = 0L;

  @Inject
  WebsocketClient(WebSocketContainer container) {
    this.container = container;
  }

  public void initialize(ConnectionConfiguration config, WebsocketClientListener listener)
      throws URISyntaxException, DeploymentException, IOException {
    this.listener = listener;
    URI endpointUri = new URI(config.getWebsocketUrl());

    logger.debug("Opening Websocket connection.");

    session = container.connectToServer(this, endpointUri);
  }

  public void sendMessage(String message) throws JsonProcessingException {
    logger.debug("Sending message, session id '{}', message '{}'.", session.getId(), message);
    session.getAsyncRemote().sendText(message);
  }

  @OnOpen
  public void onOpen(Session session) {
    logger.debug("Opened Websocket, session id '{}'.", session.getId());
  }

  @OnClose
  public void onClose(Session userSession, CloseReason reason) {
    logger.debug("Closed Websocket session: code '{}', message '{}'.",
        reason.getCloseCode().getCode(), reason.getReasonPhrase());
    listener.onWebsocketClose(closedByClient);
  }

  @OnMessage
  public void onMessage(String message) {
    logger.trace("Received Websocket message: '{}'.", message);

    // First successful message is '{"server_id":"0"}' - just discard.
    if (receivedMessages == 0) {
      receivedMessages++;
      if (message.contains("error")) {
        logger.error("Closing connection due to receiving an error initiating the session: {}",
            message);
        try {
          close();
        } catch (IOException e) {
          logger.error("Failed to close session; still informing listener!", e);
          listener.onWebsocketClose(true);
        }
      }
      return;
    }

    listener.onWebsocketMessage(message);
  }

  public void close() throws IOException {
    if (!session.isOpen()) {
      return;
    }

    logger.debug("Closing Websocket, session id '{}'.", session.getId());

    closedByClient = true;
    session.close();
  }

}
