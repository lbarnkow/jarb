package bot.rocketchat.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import bot.ConnectionInfo;
import bot.rocketchat.rest.responses.ChannelListResponse;
import bot.rocketchat.rest.responses.ChannelListResponse.Channel;
import bot.rocketchat.rest.responses.SubscriptionsGetResponse;
import bot.rocketchat.rest.responses.SubscriptionsGetResponse.Subscription;
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

	public List<String> getSubscriptions() {
		Response response = buildRequest("subscriptions.get", new QueryParam("count", 0)).get();
		List<Subscription> updated = new ArrayList<>();

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			updated = response.readEntity(SubscriptionsGetResponse.class).getUpdated();

		return updated.stream().map(sub -> sub.getRoomId()).collect(Collectors.toList());
	}

	public List<String> getChannels() {
		Response response = buildRequest("channels.list", new QueryParam("count", 0)).get();
		List<Channel> channels = new ArrayList<>();

		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			channels = response.readEntity(ChannelListResponse.class).getChannels();

		return channels.stream().map(channel -> channel.getId()).collect(Collectors.toList());
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
