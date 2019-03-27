package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.api.MessageType.USER_JOINED_ROOM;
import static io.github.lbarnkow.jarb.api.RoomType.PUBLIC_CHANNEL;

import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.User;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RawMessageTest {

  @Test
  void testConvertWith() {
    // given
    RawUser rawUser = RawUser.builder().id("id").username("username").build();
    Room room = Room.builder().id("id").name("name").type(PUBLIC_CHANNEL).build();

    RawMessage rawMessage = RawMessage.builder() //
        .id("id") //
        .rid(room.getId()) //
        .msg("message") //
        .ts(Instant.now().toString()) //
        .user(rawUser) //
        .attachments(Collections.emptyList()) //
        .type(USER_JOINED_ROOM.getRawType()) //
        .build();

    // when
    Message message = rawMessage.convertWith(room);

    // then
    assertThat(message.getId()).isEqualTo(rawMessage.getId());
    assertThat(message.getRoom()).isSameAs(room);
    assertThat(message.getRoom().getId()).isEqualTo(rawMessage.getRid());
    assertThat(message.getMessage()).isEqualTo(rawMessage.getMsg());
    assertThat(message.getTimestamp().toString()).isEqualTo(rawMessage.getTs());
    assertThat(message.getUser().getId()).isEqualTo(rawMessage.getUser().getId());
    assertThat(message.getUser().getName()).isEqualTo(rawMessage.getUser().getUsername());
    assertThat(message.getAttachments()).isEmpty();
    assertThat(rawMessage.getAttachments()).isEmpty();
    assertThat(message.getType().getRawType()).isEqualTo(rawMessage.getType());
  }

  @Test
  void testOf() {
    // given
    Room room = Room.builder().id("id").name("name").type(PUBLIC_CHANNEL).build();
    User user = User.builder().id("id").name("name").build();

    Message message = Message.builder() //
        .id("id") //
        .room(room) //
        .message("message") //
        .timestamp(Instant.now()) //
        .user(user) //
        .attachments(Collections.emptyList()) //
        .build();

    // when
    RawMessage rawMessage = RawMessage.of(message);

    // then
    assertThat(message.getId()).isEqualTo(rawMessage.getId());
    assertThat(message.getRoom()).isSameAs(room);
    assertThat(message.getRoom().getId()).isEqualTo(rawMessage.getRid());
    assertThat(message.getMessage()).isEqualTo(rawMessage.getMsg());
    assertThat(message.getTimestamp().toString()).isEqualTo(rawMessage.getTs());
    assertThat(message.getUser().getId()).isEqualTo(rawMessage.getUser().getId());
    assertThat(message.getUser().getName()).isEqualTo(rawMessage.getUser().getUsername());
    assertThat(message.getAttachments()).isEmpty();
    assertThat(rawMessage.getAttachments()).isEmpty();
  }
}
