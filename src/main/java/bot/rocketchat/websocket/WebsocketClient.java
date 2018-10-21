package bot.rocketchat.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
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

	private final WebsocketClientListener listener;
	private final Session session;

	private boolean closedByClient = false;

	public WebsocketClient(String uri, WebsocketClientListener listener)
			throws URISyntaxException, DeploymentException, IOException {
		this.listener = listener;
		URI endpointUri = new URI(uri);

		logger.debug("Opening Websocket connection.");

		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
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
