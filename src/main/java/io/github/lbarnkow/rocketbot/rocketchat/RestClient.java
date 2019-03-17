package io.github.lbarnkow.rocketbot.rocketchat;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.api.Bot.AuthInfo;
import io.github.lbarnkow.rocketbot.api.Room;
import io.github.lbarnkow.rocketbot.api.RoomType;
import io.github.lbarnkow.rocketbot.misc.Common;
import io.github.lbarnkow.rocketbot.rocketchat.rest.RestClientException;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.SubscriptionsGetOneReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.SubscriptionsReadReply;
import io.github.lbarnkow.rocketbot.rocketchat.rest.messages.SubscriptionsUnreadReply;

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

	public ChannelListReply getChannelList(Bot bot) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

		Response response = buildRequest(bot, "channels.list", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(ChannelListReply.class);
	}

	public ChannelListJoinedReply getChannelListJoined(Bot bot) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

		Response response = buildRequest(bot, "channels.list.joined", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(ChannelListJoinedReply.class);
	}

	public SubscriptionsGetOneReply getOneSubscription(Bot bot, String roomId) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

		Response response = buildRequest(bot, "subscriptions.getOne", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(SubscriptionsGetOneReply.class);
	}

	public ChatCountersReply getChatCounters(Bot bot, RoomType roomType, String roomId) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

		String endpointBase = selectRestEndpointBase(roomType);
		Response response = buildRequest(bot, endpointBase + ".counters", params).get();

		handleBadHttpResponseCodes(response);

		return response.readEntity(ChatCountersReply.class);
	}

	public ChatHistoryReply getChatHistory(Bot bot, Room room, Instant latest, Instant oldest, boolean inclusive)
			throws RestClientException {
		List<QueryParam> params = Arrays.asList( //
				new QueryParam("roomId", room.getId()), //
				new QueryParam("latest", latest.toString()), //
				new QueryParam("oldest", oldest.toString()), //
				new QueryParam("inclusive", Boolean.toString(inclusive)), //
				new QueryParam("count", 0));

		String endpointBase = selectRestEndpointBase(room.getType());
		Response response = buildRequest(bot, endpointBase + ".history", params).get();

		handleBadHttpResponseCodes(response);

		return response.readEntity(ChatHistoryReply.class);
	}

	public SubscriptionsReadReply markSubscriptionRead(Bot bot, String roomId) throws RestClientException {
		Map<String, String> payload = Collections.singletonMap("rid", roomId);

		Entity<Map<String, String>> body = Entity.entity(payload, APPLICATION_JSON);
		Response response = buildRequest(bot, "subscriptions.read", emptyList()).post(body);

		handleBadHttpResponseCodes(response);

		return response.readEntity(SubscriptionsReadReply.class);
	}

	public static class Bla extends Common {
		@SuppressWarnings("unused")
		private Inner firstUnreadMessage;

		public Bla(String _id) {
			this.firstUnreadMessage = new Inner(_id);
		}
	}

	public static class Inner extends Common {
		@SuppressWarnings("unused")
		private String _id;

		public Inner(String _id) {
			this._id = _id;
		}
	}

	public SubscriptionsUnreadReply markSubscriptionUnread(Bot bot, String messageId) throws RestClientException {
//		Map<String, String> inner = Collections.singletonMap("_id", messageId);
//		Map<String, Map<String, String>> payload = Collections.singletonMap("firstUnreadMessage", inner);
		Bla payload = new Bla(messageId);

		Entity<Bla> body = Entity.entity(payload, APPLICATION_JSON);
		Response response = buildRequest(bot, "subscriptions.unread", emptyList()).post(body);

		handleBadHttpResponseCodes(response);

		return response.readEntity(SubscriptionsUnreadReply.class);
	}

	private String selectRestEndpointBase(RoomType roomType) {
		switch (roomType) {
		case PUBLIC_CHANNEL:
			return "channels";
		case PRIVATE_GROUP:
			return "groups";
		case INSTANT_MESSAGE:
			return "im";
		default:
			throw new IllegalArgumentException("Unrecognized room type '" + roomType + "'!");
		}
	}

	private void handleBadHttpResponseCodes(Response response) throws RestClientException {
		if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
			throw new RestClientException(response.readEntity(String.class));
		}
	}

	private Builder buildRequest(Bot bot, String path, List<QueryParam> params) {
		WebTarget target = baseTarget;

		for (QueryParam param : params) {
			target = target.queryParam(param.getKey(), param.getValues());
		}

		return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders(bot));
	}

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
