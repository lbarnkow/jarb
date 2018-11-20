package bot.rocketchat.websocket;

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

import bot.ConnectionInfo;

@ClientEndpoint
public class WebsocketClient {
	private static final Logger logger = LoggerFactory.getLogger(WebsocketClient.class);

	@Inject
	private WebsocketContainerProvider websocketContainerProvider;
	@Inject
	private ConnectionInfo conInfo;

	private boolean initialized;

	private WebsocketClientListener listener;
	private Session session;

	private boolean closedByClient = false;

	WebsocketClient() {
	}

	public void initialize(WebsocketClientListener listener)
			throws URISyntaxException, DeploymentException, IOException {
		if (this.initialized)
			throw new IllegalStateException("Message already initialized!");

		this.listener = listener;
		URI endpointUri = new URI(conInfo.getWebsocketUrl());

		logger.debug("Opening Websocket connection.");

		WebSocketContainer container = websocketContainerProvider.get();
		session = container.connectToServer(this, endpointUri);
	}

	public void sendMessage(Object msg) {
		String message = msg.toString();
		logger.debug("Sending message, session id '{}', message '{}'.", session.getId(), message);
		session.getAsyncRemote().sendText(message);
	}

	@OnOpen
	public void onOpen(Session session) {
		logger.debug("Opened Websocket, session id '{}'.", session.getId());
	}

	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		logger.debug("Closed Websocket session: code '{}', message '{}'.", reason.getCloseCode().getCode(),
				reason.getReasonPhrase());
		listener.onWebsocketClose(closedByClient);
	}

	@OnMessage
	public void onMessage(String message) {
		logger.debug("Received Websocket message: '{}'.", message);

		if (message.contains("server_id"))
			return;

		listener.onWebsocketMessage(message);
	}

	public void close() throws IOException {
		if (!session.isOpen())
			return;

		logger.debug("Closing Websocket, session id '{}'.", session.getId());

		closedByClient = true;
		session.close();
	}
}
