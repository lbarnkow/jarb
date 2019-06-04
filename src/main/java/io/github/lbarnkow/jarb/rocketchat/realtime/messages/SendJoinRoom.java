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

/**
 * See
 * https://rocket.chat/docs/developer-guides/realtime-api/method-calls/joining-channels/
 * .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendJoinRoom extends BaseMethod {
  /**
   * The method name.
   */
  private static final String METHOD = "joinRoom";

  /**
   * The parameters.
   */
  private final List<String> params = new ArrayList<>();

  /**
   * The "joinRoom" message for the real-time API.
   *
   * @param roomId   id of the room to join
   * @param joinCode an optional join code for protected rooms
   */
  public SendJoinRoom(final String roomId, final String joinCode) {
    super(METHOD);

    this.params.add(roomId);
    if (joinCode != null) {
      this.params.add(joinCode);
    }
  }

  /**
   * Constructs a new instance.
   *
   * @param room the <code>Room</code> to join
   */
  public SendJoinRoom(final Room room) {
    this(room.getId(), null);
  }
}
