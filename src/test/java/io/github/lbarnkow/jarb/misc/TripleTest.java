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
