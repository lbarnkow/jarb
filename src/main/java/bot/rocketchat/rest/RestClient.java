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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.ConnectionInfo;
import bot.rocketchat.rest.responses.ChannelListResponse;
import bot.rocketchat.rest.responses.ChannelListResponse.Channel;
import bot.rocketchat.rest.responses.GroupListResponse;
import bot.rocketchat.rest.responses.GroupListResponse.Group;
import bot.rocketchat.rest.responses.ImListResponse;
import bot.rocketchat.rest.responses.ImListResponse.Im;
import bot.rocketchat.util.GsonJerseyProvider;
import bot.rocketchat.util.ObjectHolder;
import bot.rocketchat.websocket.messages.RecLogin;

public class RestClient {

	private static final Logger logger = LoggerFactory.getLogger(RestClient.class);

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

	private Builder buildRequest(String path) {
		return buildRequest(path, -1);
	}

	private Builder buildRequest(String path, int count) {
		WebTarget target = baseTarget;

		if (count < 0)
			target.queryParam("count", count);

		return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders());
	}

	public List<String> getRoomIds() {
		List<String> ids = new ArrayList<>();

		ids.addAll(getChannelIds());
		ids.addAll(getGroupIds());
		ids.addAll(getImIds());

		return ids;
	}

	private List<String> getChannelIds() {
		Response response = buildRequest("channels.list", 0).get();
		List<Channel> channels = new ArrayList<>();

		System.out.println(response.getStatus());
		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			channels = response.readEntity(ChannelListResponse.class).getChannels();

		return channels.stream().map(channel -> channel.getId()).collect(Collectors.toList());
	}

	private List<String> getGroupIds() {
		Response response = buildRequest("groups.list", 0).get();
		List<Group> groups = new ArrayList<>();

		System.out.println(response.getStatus());
		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			groups = response.readEntity(GroupListResponse.class).getGroups();

		return groups.stream().map(group -> group.getId()).collect(Collectors.toList());
	}

	private List<String> getImIds() {
		Response response = buildRequest("im.list", 0).get();
		List<Im> ims = new ArrayList<>();

		System.out.println(response.getStatus());
		if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL)
			ims = response.readEntity(ImListResponse.class).getIms();

		return ims.stream().map(im -> im.getId()).collect(Collectors.toList());
	}
}
