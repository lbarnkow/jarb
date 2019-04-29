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

package io.github.lbarnkow.jarb.bots.pugme;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.api.MessageType.REGULAR_CHAT_MESSAGE;
import static io.github.lbarnkow.jarb.api.MessageType.ROOM_NAME_CHANGED;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbarnkow.jarb.api.Attachment;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.User;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PugMeBotTest {
  private static final String TEST_BOTNAME = "PugMeBot";
  private static final String TEST_USERNAME = "pugme";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Client jerseyMock = mock(Client.class);
  private final WebTarget targetMock = mock(WebTarget.class);
  private final Builder builderMock = mock(Invocation.Builder.class);
  private final StatusType typeMock = mock(StatusType.class);
  private final Response responseMock = mock(Response.class);

  private final PugMeBot bot = new PugMeBot(new Random(), jerseyMock);

  @BeforeEach
  void beforeEach() {
    when(jerseyMock.target(anyString())).thenReturn(targetMock);
    when(targetMock.queryParam(any(), any())).thenReturn(targetMock);
    when(targetMock.path(anyString())).thenReturn(targetMock);
    when(targetMock.request(eq(APPLICATION_JSON))).thenReturn(builderMock);
    when(builderMock.get()).thenReturn(responseMock);
    when(responseMock.getStatusInfo()).thenReturn(typeMock);
    when(typeMock.getFamily()).thenReturn(SUCCESSFUL);
    when(responseMock.readEntity(RedditResponse.class)).then(invocation -> {
      return loadTestData();
    });

    bot.initialize(TEST_BOTNAME, TEST_USERNAME);
  }

  @Test
  void testOfferRoom() {
    // given

    // when
    boolean response = bot.offerRoom(Room.builder().build());

    // then
    assertThat(response).isFalse();
  }

  @Test
  void testOfferMessageWithBadType() {
    // given
    Message message = Message.builder().type(ROOM_NAME_CHANGED).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isFalse();
  }

  @Test
  void testOfferMessageFromPugMeBot() {
    // given
    User self = User.builder().name(TEST_USERNAME).build();
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(self).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isFalse();
  }

  @Test
  void testBadHttpResponseStatusCode() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME;
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();
    when(typeMock.getFamily()).thenReturn(SERVER_ERROR);

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    assertThat(response.get().getText()).contains("error");
  }

  @Test
  void testOfferMessageSimpleReques() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + "    ";
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(1);
    assertThat(attachments.get(0).getImageUrl()).isNotNull();
    assertThat(attachments.get(0).getText()).isNull();
  }

  @Test
  void testOfferMessageBombRequest() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + " bomb";
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(5);
    attachments.forEach(item -> assertThat(item.getImageUrl()).isNotNull());
    attachments.forEach(item -> assertThat(item.getText()).isNull());
  }

  @Test
  void testOfferMessageBombRequestWithNumberZero() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + " bomb 0";
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(1);
    attachments.forEach(item -> assertThat(item.getImageUrl()).isNotNull());
    attachments.forEach(item -> assertThat(item.getText()).isNull());
  }

  @Test
  void testOfferMessageBombRequestWithNumber() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + " bomb 20";
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(20);
    attachments.forEach(item -> assertThat(item.getImageUrl()).isNotNull());
    attachments.forEach(item -> assertThat(item.getText()).isNull());
  }

  @Test
  void testOfferMessageBombRequestWithHugeNumber() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + " bomb " + Integer.MAX_VALUE;
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(PugMeBot.MAX_PUGS_PER_POST);
    attachments.forEach(item -> assertThat(item.getImageUrl()).isNotNull());
    attachments.forEach(item -> assertThat(item.getText()).isNull());
  }

  @Test
  void testOfferMessageHelp() {
    // given
    User user = User.builder().name(randomUUID().toString()).build();
    String text = "@" + TEST_USERNAME + " help";
    Message message = Message.builder().type(REGULAR_CHAT_MESSAGE).user(user).text(text).build();

    // when
    Optional<Message> response = bot.offerMessage(message);

    // then
    assertThat(response.isPresent()).isTrue();
    List<Attachment> attachments = response.get().getAttachments();
    assertThat(attachments).isNotNull();
    assertThat(attachments).hasSize(1);
    assertThat(attachments.get(0).getText()).isNotNull();
    assertThat(attachments.get(0).getImageUrl()).isNull();
  }

  private RedditResponse loadTestData() {
    InputStream stream = getClass().getResourceAsStream("pugs.json");
    try {
      return MAPPER.readValue(stream, RedditResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
