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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * See https://rocket.chat/docs/developer-guides/realtime-api/ .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@NoArgsConstructor
public class BaseMessage {
  /**
   * The type of communication ('method' or 'sub').
   */
  private String msg;

  /**
   * A unique id matching up a request/response pair.
   */
  private String id;

  /**
   * Used in responses, not to be sent in requests.
   */
  private String collection;

  /**
   * Used in responses, not to be sent in requests.
   */
  private Error error;

  /**
   * Constructs a new instance.
   * 
   * @param msg The type of communication ('method' or 'sub')
   * @param id  The unique id matching up a request/response pair
   */
  BaseMessage(String msg, String id) {
    this.msg = msg;
    this.id = id;
  }

  /**
   * Constructs a new instance (with a random id).
   * 
   * @param msg The type of communication ('method' or 'sub')
   */
  BaseMessage(String msg) {
    this(msg, null);
  }

  /**
   * Error structure, used in responses.
   * 
   * @author lbarnkow
   */
  @JarbJsonSettings
  @Data
  public static class Error {
    /**
     * Purpose unknown.
     */
    private boolean isClientSafe;

    /**
     * The error code.
     */
    private int error;

    /**
     * The reason.
     */
    private String reason;

    /**
     * The error message.
     */
    private String message;

    /**
     * The error type.
     */
    private String errorType;
  }
}
