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

package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * See https://rocket.chat/docs/developer-guides/realtime-api/ .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseMethod extends BaseMessage {
  /**
   * The type of communication ('method'!).
   */
  private static final String MSG = "method";

  /**
   * The method.
   */
  private String method;

  /**
   * Constructs a new instance.
   *
   * @param id     The unique id matching up a request/response pair
   * @param method The method
   */
  BaseMethod(final String id, final String method) {
    super(MSG, id);
    this.method = method;
  }

  /**
   * Constructs a new instance (with a random id).
   *
   * @param method The method
   */
  public BaseMethod(final String method) {
    this(UUID.randomUUID().toString(), method);
  }
}
