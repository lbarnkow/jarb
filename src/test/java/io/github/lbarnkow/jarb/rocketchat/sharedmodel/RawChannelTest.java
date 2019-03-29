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
