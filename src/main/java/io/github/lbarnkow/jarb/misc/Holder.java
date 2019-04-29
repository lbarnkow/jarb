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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * <code>Holder</code> is an update-able object reference. This way dependent
 * systems can always reference the most recent object through this holder
 * without resorting to notifications via event systems and observer patterns.
 *
 * @author lbarnkow
 *
 * @param <T> type of the reference to hold
 */
@ToString
@EqualsAndHashCode
public class Holder<T> {
  /**
   * The initial value assigned to this instance upon construction.
   */
  private final transient T initialValue;

  /**
   * The current value assigned to this instance.
   */
  @NonNull
  @Getter
  @Setter
  private T value;

  /**
   * Constructs a new instance with a given initial value.
   *
   * @param value the inital value.
   */
  public Holder(T value) {
    this.initialValue = value;
    this.value = value;
  }

  /**
   * Resets this instance to the initial value supplied upon construction.
   */
  public void reset() {
    setValue(initialValue);
  }
}
