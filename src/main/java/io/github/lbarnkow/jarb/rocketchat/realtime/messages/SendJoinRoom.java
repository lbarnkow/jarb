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
import io.github.lbarnkow.jarb.api.Room;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendJoinRoom extends BaseMessageWithMethod {
  private static final String METHOD = "joinRoom";

  private final List<String> params = new ArrayList<>();

  /**
   * The "joinRoom" message for the real-time API.
   *
   * @param roomId   id of the room to join
   * @param joinCode an optional join code for protected rooms
   */
  public SendJoinRoom(String roomId, String joinCode) {
    super(METHOD);

    this.params.add(roomId);
    if (joinCode != null) {
      this.params.add(joinCode);
    }
  }

  public SendJoinRoom(Room room) {
    this(room.getId(), null);
  }
}
