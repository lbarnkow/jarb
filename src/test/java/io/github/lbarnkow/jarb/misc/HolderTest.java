package io.github.lbarnkow.jarb.misc;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class HolderTest {

  @Test
  void testReset() {
    // given
    String initial = "initial";
    Holder<String> holder = new Holder<>(initial);
    holder.setValue("other");
    assertThat(holder.getValue()).isNotEqualTo(initial);

    // when
    holder.reset();

    // then
    assertThat(holder.getValue()).isEqualTo(initial);
  }

}
