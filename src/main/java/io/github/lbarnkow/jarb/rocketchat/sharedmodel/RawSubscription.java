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
import lombok.Data;

/**
 * See
 * https://rocket.chat/docs/developer-guides/realtime-api/method-calls/get-subscriptions/
 * #subscription-object .
 * 
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
public class RawSubscription {
  /**
   * The room type (the same used on the [room object][1]).
   */
  @JsonProperty("t")
  private String type;

  /**
   * Timestamp the room was created at, so this should equal the room’s ts field.
   */
  @SuppressWarnings("PMD.ShortVariable")
  private RawDate ts;

  /**
   * Last seen timestamp (The last time the user has seen a message in the room).
   */
  @SuppressWarnings("PMD.ShortVariable")
  private RawDate ls;

  /**
   * The room name.
   */
  private String name;

  /**
   * The room id.
   */
  private String rid;

  /**
   * An simple user object with its id and username.
   */
  @JsonProperty("u")
  private RawUser user;

  /**
   * Whether the room the subscription is for has been opened or not (defaults to
   * false on direct messages). This is used in the clients to determine whether
   * the user can see this subscription in their list, since you can hide rooms
   * from being visible without leaving them.
   */
  private boolean open;

  /**
   * Whether there is an alert to be displayed to the user.
   */
  private boolean alert;

  // roles?

  /**
   * The total of unread messages.
   */
  private int unread;

  /**
   * Timestamp of when the subscription record was updated.
   */
  @JsonProperty("_updatedAt")
  private RawDate updatedAt;

  /**
   * The subscription id.
   */
  @JsonProperty("_id")
  @SuppressWarnings("PMD.ShortVariable")
  private String id;
}
