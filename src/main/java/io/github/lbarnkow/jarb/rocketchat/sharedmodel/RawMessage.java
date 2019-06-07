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
/**
 * See
 * https://rocket.chat/docs/developer-guides/realtime-api/the-message-object/ .
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawMessage {
  /**
   * The message id.
   */
  @JsonProperty("_id")
  @SuppressWarnings("PMD.ShortVariable")
  private String id;

  /**
   * The room id - Identify the room the message belongs.
   */
  private String rid;

  /**
   * The textual message.
   */
  private String msg;

  /**
   * The message time stamp (date of creation on client).
   */
  @SuppressWarnings("PMD.ShortVariable")
  private String ts;

  /**
   * The user that sent the message.
   */
  @JsonProperty("u")
  private RawUser user;

  /**
   * The time stamp when the message got saved on the server.
   */
  @JsonProperty("_updatedAt")
  private String updatedAt;

  /**
   * (Optional) The time stamp of when the message was edited.
   */
  private String editedAt;

  /**
   * (Optional) The user that edited the message.
   */
  private String editedBy;

  // urls: (Optional) A collection of URLs metadata. Available when the message
  // contains at least one URL

  /**
   * (Optional) A collection of attachment objects, available only when the
   * message has at least one attachment.
   */
  @Builder.Default
  private List<RawAttachment> attachments = Collections.emptyList();

  // alias: (Optional) A way to display the message is “sent” from someone else
  // other than the user who sent the message

  /**
   * (Optional) A url to an image, that is accessible to anyone, to display as the
   * avatar instead of the message user’s account avatar.
   */
  private String avatar;

  /**
   * (Optional) Boolean that states whether or not this message should be grouped
   * together with other messages from the same user.
   */
  private boolean groupable;

  /**
   * (Optional) Whether Rocket.Chat should try and parse the urls or not.
   */
  private boolean parseUrls;

  /**
   * The message type.
   */
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
  public Message convertWith(final Room room) {
    return Message.builder() //
        .id(id) //
        .room(room) //
        .text(msg) //
        .timestamp(ts == null ? null : Instant.parse(ts)) //
        .user(user.convert()) //
        .attachments(RawAttachment.convertList(attachments)) //
        .type(MessageType.parse(type)) //
        .build();
  }

  /**
   * Converts a <code>Message</code> instance to a <code>RawMessage</code>
   * instance.
   *
   * @param message the <code>Message</code> instance to convert
   * @return the resulting <code>RawMessage</code>
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RawMessage of(final Message message) {
    return RawMessage.builder() //
        .id(message.getId()) //
        .rid(message.getRoom().getId()) //
        .msg(message.getText()) //
        .ts(message.getTimestamp() == null ? null : message.getTimestamp().toString()) //
        .user(message.getUser() == null ? null : RawUser.of(message.getUser())) //
        .attachments(RawAttachment.of(message.getAttachments())) //
        .build();
  }
}