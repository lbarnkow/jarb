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
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendLogin extends BaseMessageWithMethod {
  private static final String METHOD = "login";

  private Params[] params;

  public SendLogin(String username, String password) {
    super(METHOD);
    this.params = new Params[] { new Params(username, password) };
  }

  @JarbJsonSettings
  @Data
  private static class Params {
    private final User user;
    private final Password password;

    public Params(String username, String password) {
      this.user = new User(username);
      this.password = new Password(password);
    }
  }

  @JarbJsonSettings
  @Data
  private static class User {
    private final String username;
  }

  @JarbJsonSettings
  @Data
  private static class Password {
    private final String digest;
    private final String algorithm = "sha-256";
  }
}
