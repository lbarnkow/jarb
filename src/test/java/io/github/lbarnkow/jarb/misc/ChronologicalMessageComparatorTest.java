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
