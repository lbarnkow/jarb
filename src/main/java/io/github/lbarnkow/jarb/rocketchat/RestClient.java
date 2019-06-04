/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb.rocketchat;

import static java.util.Collections.emptyList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatCountersReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChatHistoryReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsGetOneReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.SubscriptionsReadReply;
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
import lombok.Synchronized;

public class RestClient {
  private final transient Client client;
  private transient WebTarget baseTarget;

  @Inject
  RestClient(final Client client) {
    this.client = client;
  }

  @Synchronized
  public void initialize(final ConnectionConfiguration config) {
    baseTarget = client.target(config.getRestUrl());
  }

  /**
   * Fetches all public channels on the server.<br>
   * <br>
   * See:
   * <a href= "https://rocket.chat/docs/developer-guides/rest-api/channels/list/">
   * https://rocket.chat/docs/developer-guides/rest-api/channels/list/</a>
   *
   * @param authInfo valid auth token
   * @return public channels on the server
   * @throws RestClientException on bad HTTP status codes
   */
  public ChannelListReply getChannelList(final AuthInfo authInfo) throws RestClientException {
    final List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

    final Response response = buildRequest(authInfo, "channels.list", params).get();
    handleBadHttpResponseCodes(response);

    return response.readEntity(ChannelListReply.class);
  }

  /**
   * Fetches all public channels a given user (see authInfo) has joined on the
   * server.<br>
   * <br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/channels/list-joined/">
   * https://rocket.chat/docs/developer-guides/rest-api/channels/list-joined/</a>
   *
   * @param authInfo valid auth token
   * @return public channels the user has joined on the server
   * @throws RestClientException on bad HTTP status codes
   */
  public ChannelListJoinedReply getChannelListJoined(final AuthInfo authInfo)
      throws RestClientException {
    final List<QueryParam> params = Arrays.asList(new QueryParam("count", 0));

    final Response response = buildRequest(authInfo, "channels.list.joined", params).get();
    handleBadHttpResponseCodes(response);

    return response.readEntity(ChannelListJoinedReply.class);
  }

  /**
   * Gets the subscription object for a given room id.<br>
   * <br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/subscriptions/getone/">
   * https://rocket.chat/docs/developer-guides/rest-api/subscriptions/getone/</a>
   *
   * @param authInfo valid auth token
   * @param roomId   the room's id
   * @return the subscription
   * @throws RestClientException on bad HTTP status codes
   */
  public SubscriptionsGetOneReply getOneSubscription(final AuthInfo authInfo, final String roomId)
      throws RestClientException {
    final List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

    final Response response = buildRequest(authInfo, "subscriptions.getOne", params).get();
    handleBadHttpResponseCodes(response);

    return response.readEntity(SubscriptionsGetOneReply.class);
  }

  /**
   * Gets the statistics / counters for a given room id and a given room type.
   * Depending on the room type three different REST calls can result, as public
   * channels, private groups and direct messages use different end points.<br>
   * <br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/channels/counters/">
   * https://rocket.chat/docs/developer-guides/rest-api/channels/counters/</a><br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/groups/counters/">
   * https://rocket.chat/docs/developer-guides/rest-api/groups/counters/</a><br>
   * See:
   * <a href= "https://rocket.chat/docs/developer-guides/rest-api/im/counters/">
   * https://rocket.chat/docs/developer-guides/rest-api/im/counters/</a>
   *
   * @param authInfo valid auth token
   * @param roomType the room's type
   * @param roomId   the room's id
   * @return the counters
   * @throws RestClientException on bad HTTP status codes
   */
  public ChatCountersReply getChatCounters(final AuthInfo authInfo, final RoomType roomType,
      final String roomId) throws RestClientException {
    final List<QueryParam> params = Arrays.asList(new QueryParam("roomId", roomId));

    final String endpointBase = selectRestEndpointBase(roomType);
    final Response response = buildRequest(authInfo, endpointBase + ".counters", params).get();

    handleBadHttpResponseCodes(response);

    return response.readEntity(ChatCountersReply.class);
  }

  /**
   * Gets messages in a given time span for a given room. Depending on the room
   * type three different REST calls can result, as public channels, private
   * groups and direct messages use different end points.<br>
   * <br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/channels/history/">
   * https://rocket.chat/docs/developer-guides/rest-api/channels/history/</a> See:
   * <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/groups/history/">
   * https://rocket.chat/docs/developer-guides/rest-api/groups/history/</a> See:
   * <a href= "https://rocket.chat/docs/developer-guides/rest-api/im/history/">
   * https://rocket.chat/docs/developer-guides/rest-api/im/history/</a>
   *
   * @param authInfo  valid auth token
   * @param room      the room
   * @param latest    start of the time window to fetch
   * @param oldest    end of the time window to fetch
   * @param inclusive <code>true</code> to include messages exactly matching start
   *                  or end of the time window to fetch
   * @return the messages with the given time frame
   * @throws RestClientException on bad HTTP status codes
   */
  public ChatHistoryReply getChatHistory(final AuthInfo authInfo, final Room room,
      final Instant latest, final Instant oldest, final boolean inclusive)
      throws RestClientException {
    final List<QueryParam> params = Arrays.asList(//
        new QueryParam("roomId", room.getId()), //
        new QueryParam("latest", latest.toString()), //
        new QueryParam("oldest", oldest.toString()), //
        new QueryParam("inclusive", Boolean.toString(inclusive)), //
        new QueryParam("count", 0));

    final String endpointBase = selectRestEndpointBase(room.getType());
    final Response response = buildRequest(authInfo, endpointBase + ".history", params).get();

    handleBadHttpResponseCodes(response);

    return response.readEntity(ChatHistoryReply.class);
  }

  /**
   * Marks a given room as read by for a given auth token (user), i.e. all
   * messages in that room up to this point in time are marked as read.<br>
   * <br>
   * See: <a href=
   * "https://rocket.chat/docs/developer-guides/rest-api/subscriptions/read/">
   * https://rocket.chat/docs/developer-guides/rest-api/subscriptions/read/</a>
   *
   * @param authInfo valid auth token
   * @param roomId   the room's id
   * @return the server's reply
   * @throws RestClientException on bad HTTP status codes
   */
  public SubscriptionsReadReply markSubscriptionRead(final AuthInfo authInfo, final String roomId)
      throws RestClientException {
    final Map<String, String> payload = Collections.singletonMap("rid", roomId);

    final Entity<Map<String, String>> body = Entity.entity(payload, APPLICATION_JSON);
    final Response response = buildRequest(authInfo, "subscriptions.read", emptyList()).post(body);

    handleBadHttpResponseCodes(response);

    return response.readEntity(SubscriptionsReadReply.class);
  }

  private String selectRestEndpointBase(final RoomType roomType) {
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

  private void handleBadHttpResponseCodes(final Response response) throws RestClientException {
    if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
      throw new RestClientException(response.readEntity(String.class));
    }
  }

  private Builder buildRequest(final AuthInfo authInfo, final String path,
      final List<QueryParam> params) {
    WebTarget target = baseTarget;

    for (final QueryParam param : params) {
      target = target.queryParam(param.getKey(), param.getValues());
    }

    return target.path(path).request(MediaType.APPLICATION_JSON).headers(authHeaders(authInfo));
  }

  private MultivaluedHashMap<String, Object> authHeaders(final AuthInfo authInfo) {
    final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.putSingle("X-User-Id", authInfo.getUserId());
    headers.putSingle("X-Auth-Token", authInfo.getAuthToken());

    return headers;
  }

  public static class QueryParam {
    private final String key;
    private final Object[] values;

    public QueryParam(final String key, final Object... values) {
      this.key = key;
      this.values = Arrays.copyOf(values, values.length);
    }

    public String getKey() {
      return key;
    }

    public Object[] getValues() {
      return values;
    }
  }
}
