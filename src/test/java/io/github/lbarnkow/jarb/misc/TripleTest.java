package io.github.lbarnkow.jarb.misc;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class TripleTest {

  @Test
  void testTriple() {
    // given
    Integer a = 5;
    Boolean b = true;
    String c = "Ok";

    // when
    Triple<Integer, Boolean, String> triple = new Triple<>(a, b, c);

    // then
    assertThat(triple.getFirst()).isSameAs(a);
    assertThat(triple.getSecond()).isSameAs(b);
    assertThat(triple.getThird()).isSameAs(c);
  }
}
