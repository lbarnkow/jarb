package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.api.RoomType.PUBLIC_CHANNEL;

import org.junit.jupiter.api.Test;

import io.github.lbarnkow.jarb.api.Room;

class RawChannelTest {

	@Test
	void testAsRoom() {
		// given
		RawChannel rawChannel = RawChannel.builder() //
				._id("id") //
				.name("name") //
				.t(PUBLIC_CHANNEL.getRawType()) //
				.build();

		// when
		Room room = rawChannel.asRoom();

		// then
		assertThat(room.getId()).isEqualTo(rawChannel.get_id());
		assertThat(room.getName()).isEqualTo(rawChannel.getName());
		assertThat(room.getType().getRawType()).isEqualTo(rawChannel.getT());
	}

	@Test
	void testOf() {
		// given
		Room room = Room.builder().id("id").name("name").type(PUBLIC_CHANNEL).build();

		// when
		RawChannel rawChannel = RawChannel.of(room);

		// then
		assertThat(room.getId()).isEqualTo(rawChannel.get_id());
		assertThat(room.getName()).isEqualTo(rawChannel.getName());
		assertThat(room.getType().getRawType()).isEqualTo(rawChannel.getT());
	}
}
