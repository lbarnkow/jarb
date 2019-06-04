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

package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * See
 * https://rocket.chat/docs/developer-guides/rest-api/authentication/login/#payload
 * .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawUser {
  /**
   * Your username or email.
   */
  @JsonProperty("_id")
  private String id;

  /**
   * Your password.
   */
  private String username;

  /**
   * Converts this instance to an <code>User</code> instance.
   *
   * @return the resulting <code>User</code>
   */
  public User convert() {
    return User.builder() //
        .id(id) //
        .name(username) //
        .build();
  }

  /**
   * Converts an <code>User</code> instance to a <code>RawUser</code> instance.
   *
   * @param u the <code>User</code> instance to convert
   * @return the resulting <code>RawUser</code>
   */
  public static RawUser of(final User u) {
    return RawUser.builder() //
        .id(u.getId()) //
        .username(u.getName()) //
        .build();
  }
}
