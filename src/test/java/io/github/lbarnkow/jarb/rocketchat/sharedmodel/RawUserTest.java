package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import static com.google.common.truth.Truth.assertThat;

import io.github.lbarnkow.jarb.api.User;
import org.junit.jupiter.api.Test;

class RawUserTest {

  @Test
  void testConvert() {
    // given
    RawUser rawUser = RawUser.builder()._id("id").username("username").build();

    // when
    User user = rawUser.convert();

    // then
    assertThat(rawUser.get_id()).isEqualTo(user.getId());
    assertThat(rawUser.getUsername()).isEqualTo(user.getName());
  }

  @Test
  void testOf() {
    // given
    User user = User.builder().id("id").name("username").build();

    // when
    RawUser rawUser = RawUser.of(user);

    // then
    assertThat(rawUser.get_id()).isEqualTo(user.getId());
    assertThat(rawUser.getUsername()).isEqualTo(user.getName());
  }
}
