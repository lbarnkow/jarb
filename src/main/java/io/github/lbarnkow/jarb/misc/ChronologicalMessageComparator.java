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

import io.github.lbarnkow.jarb.api.Message;
import java.util.Comparator;

/**
 * A comparator to sort chat messages according to their time stamps.
 *
 * @author lbarnkow
 */
public class ChronologicalMessageComparator implements Comparator<Message> {
  /**
   * Static thread-safe instance of this comparator.
   */
  public static final ChronologicalMessageComparator CHRONOLOGICAL_MESSAGE_COMPARATOR =
      new ChronologicalMessageComparator();

  @Override
  public int compare(Message o1, Message o2) {
    return o1.getTimestamp().compareTo(o2.getTimestamp());
  }
}
