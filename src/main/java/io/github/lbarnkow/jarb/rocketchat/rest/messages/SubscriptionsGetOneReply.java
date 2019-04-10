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

package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Set https://rocket.chat/docs/developer-guides/rest-api/subscriptions/getone/
 * .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionsGetOneReply extends BaseReply {
  /**
   * The room id.
   */
  private String rid;

  /**
   * The room / subscription name.
   */
  private String name;

  /**
   * The room type.
   */
  @JsonProperty("t")
  private String type;
}
