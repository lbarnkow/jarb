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
import io.github.lbarnkow.jarb.api.Credentials;
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
public class SendLogin extends BaseMethod {
  /**
   * The method name.
   */
  private static final String METHOD = "login";

  /**
   * The parameters.
   */
  private Params[] params;

  /**
   * Constructs a new instance.
   * 
   * @param credentials the <code>Credentials</code> to use for log ins
   */
  public SendLogin(Credentials credentials) {
    super(METHOD);
    this.params = new Params[] { new Params(credentials) };
  }

  /**
   * Login method parameters.
   * 
   * @author lbarnkow
   */
  @JarbJsonSettings
  @Data
  private static class Params {
    /**
     * The user.
     */
    private final User user;

    /**
     * The password.
     */
    private final Password password;

    /**
     * Constructs a new instance.
     * 
     * @param credentials the <code>Credentials</code> to use for log ins
     */
    public Params(Credentials credentials) {
      this.user = new User(credentials.getUsername());
      this.password = new Password(credentials.getPassword());
    }
  }

  /**
   * Login method User.
   * 
   * @author lbarnkow
   */
  @JarbJsonSettings
  @Data
  private static class User {
    /**
     * The user name.
     */
    private final String username;
  }

  /**
   * Login method Password.
   * 
   * @author lbarnkow
   */
  @JarbJsonSettings
  @Data
  private static class Password {
    /**
     * The password hash.
     */
    private final String digest;

    /**
     * The hashing algorithm.
     */
    private final String algorithm = "sha-256";
  }
}
