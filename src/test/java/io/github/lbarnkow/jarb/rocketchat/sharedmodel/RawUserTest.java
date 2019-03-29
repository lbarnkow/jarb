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

import io.github.lbarnkow.jarb.api.User;
import org.junit.jupiter.api.Test;

class RawUserTest {

  @Test
  void testConvert() {
    // given
    RawUser rawUser = RawUser.builder().id("id").username("username").build();

    // when
    User user = rawUser.convert();

    // then
    assertThat(rawUser.getId()).isEqualTo(user.getId());
    assertThat(rawUser.getUsername()).isEqualTo(user.getName());
  }

  @Test
  void testOf() {
    // given
    User user = User.builder().id("id").name("username").build();

    // when
    RawUser rawUser = RawUser.of(user);

    // then
    assertThat(rawUser.getId()).isEqualTo(user.getId());
    assertThat(rawUser.getUsername()).isEqualTo(user.getName());
  }
}
