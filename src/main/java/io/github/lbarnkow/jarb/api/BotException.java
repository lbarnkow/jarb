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

package io.github.lbarnkow.jarb.api;

/**
 * Custom run-time exception to be used by bots.
 *
 * @author lbarnkow
 */
public class BotException extends RuntimeException {
  private static final long serialVersionUID = -2319589237973725608L;

  /**
   * Constructs a new instance.
   *
   * @param message the error message
   */
  public BotException(final String message) {
    super(message);
  }

  /**
   * Constructs a new instance.
   *
   * @param message the error message
   * @param cause   the root cause
   */
  public BotException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
