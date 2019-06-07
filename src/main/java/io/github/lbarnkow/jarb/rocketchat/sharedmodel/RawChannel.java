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
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * See https://rocket.chat/docs/developer-guides/rest-api/channels/list/ .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawChannel {
  /**
   * The channel id.
   */
  @JsonProperty("_id")
  @SuppressWarnings("PMD.ShortVariable")
  private String id;

  /**
   * The channel name.
   */
  private String name;

  /**
   * The channel type.
   */
  @JsonProperty("t")
  private String type;

  /**
   * Converts this instance to a <code>Room</code> instance.
   *
   * @return the resulting <code>Room</code>
   */
  public Room convert() {
    final RoomType type = RoomType.parse(this.type);
    return Room.builder().id(id).name(name).type(type).build();
  }

  /**
   * Converts an <code>Room</code> instance to a <code>RawChannel</code> instance.
   *
   * @param room the <code>Room</code> instance to convert
   * @return the resulting <code>RawChannel</code>
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RawChannel of(final Room room) {
    final RawChannel channel = new RawChannel();
    channel.id = room.getId();
    channel.name = room.getName();
    channel.type = room.getType().getRawType();
    return channel;
  }
}
