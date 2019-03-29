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
import static java.time.temporal.ChronoUnit.DAYS;

import io.github.lbarnkow.jarb.api.Message;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ChronologicalMessageComparatorTest {

  private Message past = Message.builder().timestamp(Instant.now().minus(1, DAYS)).build();
  private Message future = Message.builder().timestamp(Instant.now().plus(1, DAYS)).build();
  private ChronologicalMessageComparator comparator =
      ChronologicalMessageComparator.CHRONOLOGICAL_MESSAGE_COMPARATOR;

  @Test
  void testComparePastAndFuture() {
    // given

    // when
    int result = comparator.compare(past, future);

    // then
    assertThat(result).isLessThan(0);
  }

  @Test
  void testCompareFutureAndPast() {
    // given

    // when
    int result = comparator.compare(future, past);

    // then
    assertThat(result).isGreaterThan(0);
  }

  @Test
  void testCompareSame() {
    // given

    // when
    int result = comparator.compare(past, past);

    // then
    assertThat(result).isEqualTo(0);
  }
}
