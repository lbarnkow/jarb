package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;
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
  @JsonAlias("_id")
  private String id;
  private String rid;
  private String msg;
  private String ts;
  @JsonAlias("u")
  private RawUser user;
  @JsonAlias("_updatedAt")
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

  @JsonAlias("t")
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