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
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * See
 * https://rocket.chat/docs/developer-guides/realtime-api/method-calls/login/ .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveLoginReply extends BaseMessage {
  /**
   * The result.
   */
  private LoginResult result;

  /**
   * See
   * https://rocket.chat/docs/developer-guides/realtime-api/method-calls/login/ .
   * 
   * @author lbarnkow
   */
  @JarbJsonSettings
  @Data
  public static class LoginResult {
    /**
     * The auth-id.
     */
    private String id;

    /**
     * The auth-token.
     */
    private String token;

    /**
     * The expiration date (unix timestamp).
     */
    private RawDate tokenExpires;

    /**
     * The login type.
     */
    private String type;
  }
}
