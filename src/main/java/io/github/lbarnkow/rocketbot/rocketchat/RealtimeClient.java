package io.github.lbarnkow.rocketbot.rocketchat;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClientListener;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.Base;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.ConnectOut;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.PongOut;

public class RealtimeClient implements WebsocketClientListener {

	private static final Logger logger = LoggerFactory.getLogger(RealtimeClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private WebsocketClient client;

	private RealtimeClientListener listener;

	@Inject
	RealtimeClient(WebsocketClient client) {
		this.client = client;
	}

	public void connect(RealtimeClientListener listener, ConnectionConfiguration config)
			throws URISyntaxException, DeploymentException, IOException {
		this.listener = listener;
		client.initialize(config, this);

		sendMessage(new ConnectOut());
		// TODO: send connect message, wait for reply
	}

	public void sendMessage(Object message) throws JsonProcessingException {
		String json = MAPPER.writeValueAsString(message);
		client.sendMessage(json);
	}

	@Override
	public void onWebsocketClose(boolean initiatedByClient) {
		listener.onRealtimeClientClose(initiatedByClient);
	}

	@Override
	public void onWebsocketMessage(String message) {
		try {
			Base baseMessage = MAPPER.readValue(message, Base.class);

			if ("connected".equals(baseMessage.getMsg())) {
				listener.onRealtimeClientSessionEstablished();
			} else if ("ping".equals(baseMessage.getMsg())) {
				sendMessage(new PongOut());
			}

		} catch (IOException e) {
			logger.error("Unexpected error deserializing server message '{}'; closing session!", message, e);
			try {
				client.close();
			} catch (IOException e1) {
				logger.error("Unexpected error disconnecting!", e1);
			}
		}
	}
}
