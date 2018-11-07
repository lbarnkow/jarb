package bot.rocketchat.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import bot.ConnectionInfo;
import bot.rocketchat.Room;
import bot.rocketchat.Subscription;
import bot.rocketchat.rest.responses.ChannelListResponse;
import bot.rocketchat.rest.responses.GenericHistoryResponse;
import bot.rocketchat.rest.responses.GenericHistoryResponse.HistoryMessage;
import bot.rocketchat.rest.responses.SubscriptionsGetResponse;
import bot.rocketchat.util.GsonJerseyProvider;
import bot.rocketchat.util.ObjectHolder;
import bot.rocketchat.websocket.messages.RecLogin;

public class RestClient {

//	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

	private final ObjectHolder<RecLogin.Result> loginTokenHolder;
	private final Client client;
	private final WebTarget baseTarget;

	public RestClient(ConnectionInfo conInfo, ObjectHolder<RecLogin.Result> loginTokenHolder) {
		this.loginTokenHolder = loginTokenHolder;
		client = ClientBuilder.newClient();
		client.register(GsonJerseyProvider.class);
		baseTarget = client.target(conInfo.getRestUrl());
	}

	private MultivaluedHashMap<String, Object> authHeaders() {
		if (loginTokenHolder.isEmpty())
			throw new IllegalStateException("No loginToken available in RestClient!");

		String userId = loginTokenHolder.get().getId();
		String userToken = loginTokenHolder.get().getToken();
		MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.putSingle("X-User-Id", userId);
		headers.putSingle("X-Auth-Token", userToken);

		return headers;
	}

	private Builder buildRequest(String path, QueryParam... params) {
		WebTarget target = baseTarget;

		for (QueryParam param : params)
			target.queryParam(param.getKey(), param.getValues());

		return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders());
	}

	public List<Subscription> getSubscriptions() {
		Response response = buildRequest("subscriptions.get", new QueryParam("count", 0)).get();
		List<Subscription> subs = new ArrayList<>();

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			subs = response.readEntity(SubscriptionsGetResponse.class).getUpdated();

		return subs;
	}

	public Subscription getOneSubscription(String roomId) {
		Response response = buildRequest("subscriptions.getOne", new QueryParam("roomId", roomId)).get();
		Subscription sub = null;

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			sub = response.readEntity(Subscription.class);

		return sub;
	}

	public List<Room> getChannels() {
		Response response = buildRequest("channels.list", new QueryParam("count", 0)).get();
		List<Room> channels = new ArrayList<>();

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			channels = response.readEntity(ChannelListResponse.class).getChannels();

		return channels;
	}

	public List<HistoryMessage> getChatHistory(Subscription sub) {
		String endpoint = selectRestEndpointBase(sub);
		Response response = buildRequest(endpoint + ".history", new QueryParam("roomId", sub.getRoomId()),
				new QueryParam("count", sub.getUnread())).get();
		List<HistoryMessage> messages = new ArrayList<>();

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			messages = response.readEntity(GenericHistoryResponse.class).getMessages();

		return messages;
	}

	private String selectRestEndpointBase(Subscription sub) {
		switch (sub.getRoomType()) {
		case CHANNEL:
			return "channels";
		case GROUP:
			return "groups";
		case IM:
			return "im";
		default:
			throw new IllegalArgumentException("Unrecognized room type '" + sub.getRoomType() + "'!");
		}
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
