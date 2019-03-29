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
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.MessageType;
import io.github.lbarnkow.jarb.api.Room;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// see https://rocket.chat/docs/developer-guides/realtime-api/the-message-object/
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawMessage {
  @JsonProperty("_id")
  private String id;
  private String rid;
  private String msg;
  private String ts;
  @JsonProperty("u")
  private RawUser user;
  @JsonProperty("_updatedAt")
  private String updatedAt;
  private String editedAt;
  private String editedBy;

  // urls: (Optional) A collection of URLs metadata. Available when the message
  // contains at least one URL

  @Builder.Default
  private List<RawAttachment> attachments = Collections.emptyList();

  // alias: (Optional) A way to display the message is “sent” from someone else
  // other than the user who sent the message

  private String avatar;
  private boolean groupable;
  private boolean parseUrls;

  @JsonProperty("t")
  private String type;

  /**
   * Converts this instance to a <code>Message</code> instance.
   *
   * @param room a <code>Room</code> to attach to the <code>Message</code>
   *             instance; needed because <code>RawMessage</code> only knows the
   *             Room-ID
   * @return the resulting <code>Message</code>
   */
  public Message convertWith(Room room) {
    return Message.builder() //
        .id(id) //
        .room(room) //
        .message(msg) //
        .timestamp(ts != null ? Instant.parse(ts) : null) //
        .user(user.convert()) //
        .attachments(RawAttachment.convertList(attachments)) //
        .type(MessageType.parse(type)) //
        .build();
  }

  /**
   * Converts a <code>Message</code> instance to a <code>RawMessage</code>
   * instance.
   *
   * @param m the <code>Message</code> instance to convert
   * @return the resulting <code>RawMessage</code>
   */
  public static RawMessage of(Message m) {
    return RawMessage.builder() //
        .id(m.getId()) //
        .rid(m.getRoom().getId()) //
        .msg(m.getMessage()) //
        .ts(m.getTimestamp() != null ? m.getTimestamp().toString() : null) //
        .user(m.getUser() != null ? RawUser.of(m.getUser()) : null) //
        .attachments(RawAttachment.of(m.getAttachments())) //
        .build();
  }
}