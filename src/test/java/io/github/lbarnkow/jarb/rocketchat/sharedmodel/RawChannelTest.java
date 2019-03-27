package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.api.RoomType.PUBLIC_CHANNEL;

import io.github.lbarnkow.jarb.api.Room;
import org.junit.jupiter.api.Test;

class RawChannelTest {

  @Test
  void testAsRoom() {
    // given
    RawChannel rawChannel = RawChannel.builder() //
        .id("id") //
        .name("name") //
        .type(PUBLIC_CHANNEL.getRawType()) //
        .build();

    // when
    Room room = rawChannel.convert();

    // then
    assertThat(room.getId()).isEqualTo(rawChannel.getId());
    assertThat(room.getName()).isEqualTo(rawChannel.getName());
    assertThat(room.getType().getRawType()).isEqualTo(rawChannel.getType());
  }

  @Test
  void testOf() {
    // given
    Room room = Room.builder().id("id").name("name").type(PUBLIC_CHANNEL).build();

    // when
    RawChannel rawChannel = RawChannel.of(room);

    // then
    assertThat(room.getId()).isEqualTo(rawChannel.getId());
    assertThat(room.getName()).isEqualTo(rawChannel.getName());
    assertThat(room.getType().getRawType()).isEqualTo(rawChannel.getType());
  }
}
