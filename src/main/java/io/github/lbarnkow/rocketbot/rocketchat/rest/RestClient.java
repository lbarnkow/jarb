package io.github.lbarnkow.rocketbot.rocketchat.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.api.Bot.AuthInfo;
import io.github.lbarnkow.rocketbot.misc.Common;
import io.github.lbarnkow.rocketbot.rocketchat.ConnectionConfiguration;
import io.github.lbarnkow.rocketbot.rocketchat.rest.model.SubscriptionsGetResponse;

public class RestClient extends Common {

//	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	private final Client client;
	private WebTarget baseTarget;

	@Inject
	RestClient(Client client) {
		this.client = client;
	}

	public synchronized void initialize(ConnectionConfiguration config) {
		baseTarget = client.target(config.getRestUrl());
	}

	public SubscriptionsGetResponse getSubscriptions(Bot bot) throws RestClientException {
		return getSubscriptions(bot, null);
	}

	public SubscriptionsGetResponse getSubscriptions(Bot bot, Instant updatedSince) throws RestClientException {
		List<QueryParam> params = new ArrayList<>();
		params.add(new QueryParam("count", 0));
		if (updatedSince != null) {
			params.add(new QueryParam("updatedSince", updatedSince.toString()));
		}

		Response response = buildRequest(bot, "subscriptions.get", params).get();

		if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
			throw new RestClientException(response.getStatusInfo());
		}

		return response.readEntity(SubscriptionsGetResponse.class);
	}

//	public Subscription getOneSubscription(String roomId) {
//		Response response = buildRequest("subscriptions.getOne", new QueryParam("roomId", roomId)).get();
//		Subscription sub = null;
//
//		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
//			sub = response.readEntity(SubscriptionsGetOneResponse.class).getSubscription();
//		else
//			throw new RuntimeException("subscriptions.getOne failed for room '" + roomId + "'! Reason: "
//					+ response.readEntity(String.class));
//
//		return sub;
//	}
//
//	public List<Room> getChannels() {
//		Response response = buildRequest("channels.list", new QueryParam("count", 0)).get();
//		List<Room> channels = new ArrayList<>();
//
//		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
//			channels = response.readEntity(ChannelListResponse.class).getChannels();
//
//		return channels;
//	}
//
//	public ChatCountersResponse getChatCounters(Room room) {
//		String endpoint = selectRestEndpointBase(room);
//		Response response = buildRequest(endpoint + ".counters", new QueryParam("roomId", room.getId())).get();
//
//		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
//			return response.readEntity(ChatCountersResponse.class);
//		else
//			throw new RuntimeException("Failed to get chat counters for room '" + room.getId() + "'. Reason: "
//					+ response.readEntity(String.class));
//	}
//
//	public List<HistoryMessage> getChatHistory(Room room, ChatCountersResponse counters) {
//		String endpoint = selectRestEndpointBase(room);
//		Response response = buildRequest(endpoint + ".history", new QueryParam("roomId", room.getId()),
//				new QueryParam("oldest", counters.getUnreadsFrom()), new QueryParam("count", 0)).get();
//		List<HistoryMessage> messages = new ArrayList<>();
//
//		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
//			messages = response.readEntity(GenericHistoryResponse.class).getMessages();
//
//		return messages;
//	}
//
//	public MessageSendResponse sendMessage(String roomId, String text, Attachment... attachments) {
//		MessageSendRequest payload = messageSendRequestProvider.get().initialize(roomId, text, attachments);
//		Response response = buildRequest("chat.postMessage").post(Entity.entity(payload, MediaType.APPLICATION_JSON));
//
//		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
//			return response.readEntity(MessageSendResponse.class);
//		else
//			throw new RuntimeException(
//					"Failed to send message to room '" + roomId + "'. Reason: " + response.readEntity(String.class));
//	}
//
//	public boolean markSubscriptionRead(String roomId) {
//		SubscriptionsReadRequest payload = subscriptionsReadRequestProvider.get().initialize(roomId);
//		Response response = buildRequest("subscriptions.read").post(Entity.entity(payload, MediaType.APPLICATION_JSON));
//
//		return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
//	}
//
//	private String selectRestEndpointBase(Room room) {
//		switch (room.getType()) {
//		case CHANNEL:
//			return "channels";
//		case GROUP:
//			return "groups";
//		case IM:
//			return "im";
//		default:
//			throw new IllegalArgumentException("Unrecognized room type '" + room.getType() + "'!");
//		}
//	}

	private Builder buildRequest(Bot bot, String path, List<QueryParam> params) {
		WebTarget target = baseTarget;

		for (QueryParam param : params) {
			target = target.queryParam(param.getKey(), param.getValues());
		}

		return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders(bot));
	}

//	private Builder buildRequest(Bot bot, String path) {
//		return buildRequest(bot, path, Collections.emptyList());
//	}

	private MultivaluedHashMap<String, Object> authHeaders(Bot bot) {
		AuthInfo authInfo = bot.getAuthHolder().get();

		MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.putSingle("X-User-Id", authInfo.getUserId());
		headers.putSingle("X-Auth-Token", authInfo.getAuthToken());

		return headers;
	}

	public static class QueryParam {
		private final String key;
		private final Object[] values;

		public QueryParam(String key, Object... values) {
			this.key = key;
			this.values = values;
		}

		public String getKey() {
			return key;
		}

		public Object[] getValues() {
			return values;
		}
	}
}
