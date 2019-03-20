package io.github.lbarnkow.jarb.rocketchat;

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

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.misc.Common;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsGetOneReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsReadReply;

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

	public ChannelListReply getChannelList(AuthInfo authInfo) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

		Response response = buildRequest(authInfo, "channels.list", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(ChannelListReply.class);
	}

	public ChannelListJoinedReply getChannelListJoined(AuthInfo authInfo) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

		Response response = buildRequest(authInfo, "channels.list.joined", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(ChannelListJoinedReply.class);
	}

	public SubscriptionsGetOneReply getOneSubscription(AuthInfo authInfo, String roomId) throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

		Response response = buildRequest(authInfo, "subscriptions.getOne", params).get();
		handleBadHttpResponseCodes(response);

		return response.readEntity(SubscriptionsGetOneReply.class);
	}

	public ChatCountersReply getChatCounters(AuthInfo authInfo, RoomType roomType, String roomId)
			throws RestClientException {
		List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

		String endpointBase = selectRestEndpointBase(roomType);
		Response response = buildRequest(authInfo, endpointBase + ".counters", params).get();

		handleBadHttpResponseCodes(response);

		return response.readEntity(ChatCountersReply.class);
	}

	public ChatHistoryReply getChatHistory(AuthInfo authInfo, Room room, Instant latest, Instant oldest,
			boolean inclusive) throws RestClientException {
		List<QueryParam> params = Arrays.asList( //
				new QueryParam("roomId", room.getId()), //
				new QueryParam("latest", latest.toString()), //
				new QueryParam("oldest", oldest.toString()), //
				new QueryParam("inclusive", Boolean.toString(inclusive)), //
				new QueryParam("count", 0));

		String endpointBase = selectRestEndpointBase(room.getType());
		Response response = buildRequest(authInfo, endpointBase + ".history", params).get();

		handleBadHttpResponseCodes(response);

		return response.readEntity(ChatHistoryReply.class);
	}

	public SubscriptionsReadReply markSubscriptionRead(AuthInfo authInfo, String roomId) throws RestClientException {
		Map<String, String> payload = Collections.singletonMap("rid", roomId);

		Entity<Map<String, String>> body = Entity.entity(payload, APPLICATION_JSON);
		Response response = buildRequest(authInfo, "subscriptions.read", emptyList()).post(body);

		handleBadHttpResponseCodes(response);

		return response.readEntity(SubscriptionsReadReply.class);
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

	private Builder buildRequest(AuthInfo authInfo, String path, List<QueryParam> params) {
		WebTarget target = baseTarget;

		for (QueryParam param : params) {
			target = target.queryParam(param.getKey(), param.getValues());
		}

		return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders(authInfo));
	}

	private MultivaluedHashMap<String, Object> authHeaders(AuthInfo authInfo) {
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
