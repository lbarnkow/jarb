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

import io.github.lbarnkow.rocketbot.misc.Tuple;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.WebsocketClientListener;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.BaseMessage;
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
	}

	public void disconnect() throws IOException {
		client.close();
	}

	public void sendMessage(Object message) throws JsonProcessingException {
		String json = MAPPER.writeValueAsString(message);
		client.sendMessage(json);
	}

	public <X> X sendMessageAndWait(BaseMessage message, long timeout, Class<X> replyType)
			throws InterruptedException, ReplyErrorException, IOException {
		if (message.getId() == null) {
			throw new IllegalArgumentException(
					"Messages must have a unique id to be used with 'sendMessageAndWait()'!");
		}

		Semaphore sem = new Semaphore(0);
		unansweredRequests.put(message.getId(), sem);

		sendMessage(message);
		sem.tryAcquire(timeout, MILLISECONDS);

		@SuppressWarnings("unchecked")
		Tuple<BaseMessage, String> reply = (Tuple<BaseMessage, String>) unansweredRequests.remove(message.getId());
		if (reply.getFirst().getError() != null) {
			throw new ReplyErrorException(reply.getFirst().getError());
		}

		return MAPPER.readValue(reply.getSecond(), replyType);
	}

	public <X> X sendMessageAndWait(BaseMessage message, Class<X> replyType)
			throws InterruptedException, ReplyErrorException, IOException {
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
			logger.debug("Unhandled message with id: {}", rawJson);
		}
	}
}
