package io.github.lbarnkow.rocketbot.rocketchat;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClientListener;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.Base;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendConnect;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendPong;

public class RealtimeClient implements WebsocketClientListener {

	private static final Logger logger = LoggerFactory.getLogger(RealtimeClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private WebsocketClient client;

	private RealtimeClientListener listener;

	private Map<String, Object> unansweredRequests = new ConcurrentHashMap<>();

	@Inject
	RealtimeClient(WebsocketClient client) {
		this.client = client;
	}

	public void connect(RealtimeClientListener listener, ConnectionConfiguration config)
			throws URISyntaxException, DeploymentException, IOException {
		this.listener = listener;
		client.initialize(config, this);

		sendMessage(new SendConnect());
		// TODO: send connect message, wait for reply
	}

	public void sendMessage(Object message) throws JsonProcessingException {
		String json = MAPPER.writeValueAsString(message);
		client.sendMessage(json);
	}

	public String sendMessageAndWait(Base message, long timeout) throws JsonProcessingException, InterruptedException {
		if (message.getId() == null) {
			throw new IllegalArgumentException(
					"Messages must have a unique id to be used with 'sendMessageAndWait()'!");
		}

		Semaphore sem = new Semaphore(0);
		unansweredRequests.put(message.getId(), sem);
		sendMessage(message);
		sem.tryAcquire(timeout, MILLISECONDS);
		return (String) unansweredRequests.remove(message.getId());
	}

	public String sendMessageAndWait(Base message) throws JsonProcessingException, InterruptedException {
		return sendMessageAndWait(message, 1000L * 60L);
	}

	@Override
	public void onWebsocketClose(boolean initiatedByClient) {
		listener.onRealtimeClientSessionClose(initiatedByClient);
	}

	@Override
	public void onWebsocketMessage(String message) {
		try {
			Base baseMessage = MAPPER.readValue(message, Base.class);

			if ("connected".equals(baseMessage.getMsg())) {
				handleConnected();
			} else if ("ping".equals(baseMessage.getMsg())) {
				handlePing();
			} else if (baseMessage.getId() != null) {
				handleMessageWithId(baseMessage, message);
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

	private void handleConnected() {
		listener.onRealtimeClientSessionEstablished();
	}

	private void handlePing() throws JsonProcessingException {
		sendMessage(new SendPong());
	}

	private void handleMessageWithId(Base message, String rawJson) {
		if (unansweredRequests.containsKey(message.getId())) {
			Semaphore semaphore = (Semaphore) unansweredRequests.remove(message.getId());
			unansweredRequests.put(message.getId(), rawJson);
			semaphore.release();
		} else {
			logger.debug("Unhandled message: {}", rawJson);
		}
	}
}
