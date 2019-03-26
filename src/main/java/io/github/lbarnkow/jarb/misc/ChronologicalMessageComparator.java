package io.github.lbarnkow.jarb.misc;

import io.github.lbarnkow.jarb.api.Message;
import java.util.Comparator;

public class ChronologicalMessageComparator implements Comparator<Message> {
  public static final ChronologicalMessageComparator CHRONOLOGICAL_MESSAGE_COMPARATOR =
      new ChronologicalMessageComparator();

  @Override
  public int compare(Message o1, Message o2) {
    return o1.getTimestamp().compareTo(o2.getTimestamp());
  }
}
