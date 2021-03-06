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

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

/**
 * Authorization information and token received from the chat server after
 * successful login.
 *
 * @author lbarnkow
 */
@Data
@Builder
public class AuthInfo {
  /**
   * An <code>AuthInfo</code> instance that is expired.
   */
  public static final AuthInfo EXPIRED = new AuthInfo(null, null, Instant.ofEpochMilli(0));

  /**
   * The user id to use in API calls.
   */
  private final String userId;

  /**
   * The authorization token to use in API calls.
   */
  private final String authToken;

  /**
   * Expiration date of this token.
   */
  private final Instant expires;

  /**
   * Checks if this token is still valid (i.e. not expired).
   *
   * @return <code>true</code> if the token hasn't expired; <code>false</code>
   *         otherwise.
   */
  public boolean isValid() {
    return expires.isAfter(Instant.now());
  }
}