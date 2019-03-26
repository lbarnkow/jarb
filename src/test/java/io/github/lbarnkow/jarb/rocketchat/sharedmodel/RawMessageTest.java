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
    RawUser rawUser = RawUser.builder()._id("id").username("username").build();
    Room room = Room.builder().id("id").name("name").type(PUBLIC_CHANNEL).build();

    RawMessage rawMessage = RawMessage.builder() //
        ._id("id") //
        .rid(room.getId()) //
        .msg("message") //
        .ts(Instant.now().toString()) //
        .u(rawUser) //
        .attachments(Collections.emptyList()) //
        .t(USER_JOINED_ROOM.getRawType()) //
        .build();

    // when
    Message message = rawMessage.convertWith(room);

    // then
    assertThat(message.getId()).isEqualTo(rawMessage.get_id());
    assertThat(message.getRoom()).isSameAs(room);
    assertThat(message.getRoom().getId()).isEqualTo(rawMessage.getRid());
    assertThat(message.getMessage()).isEqualTo(rawMessage.getMsg());
    assertThat(message.getTimestamp().toString()).isEqualTo(rawMessage.getTs());
    assertThat(message.getUser().getId()).isEqualTo(rawMessage.getU().get_id());
    assertThat(message.getUser().getName()).isEqualTo(rawMessage.getU().getUsername());
    assertThat(message.getAttachments()).isEmpty();
    assertThat(rawMessage.getAttachments()).isEmpty();
    assertThat(message.getType().getRawType()).isEqualTo(rawMessage.getT());
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
    assertThat(message.getId()).isEqualTo(rawMessage.get_id());
    assertThat(message.getRoom()).isSameAs(room);
    assertThat(message.getRoom().getId()).isEqualTo(rawMessage.getRid());
    assertThat(message.getMessage()).isEqualTo(rawMessage.getMsg());
    assertThat(message.getTimestamp().toString()).isEqualTo(rawMessage.getTs());
    assertThat(message.getUser().getId()).isEqualTo(rawMessage.getU().get_id());
    assertThat(message.getUser().getName()).isEqualTo(rawMessage.getU().getUsername());
    assertThat(message.getAttachments()).isEmpty();
    assertThat(rawMessage.getAttachments()).isEmpty();
  }
}
