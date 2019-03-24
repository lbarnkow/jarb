package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.MessageType;
import io.github.lbarnkow.jarb.api.Room;
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
	private String _id;
	private String rid;
	private String msg;
	private String ts;
	private RawUser u;
	private String _updatedAt;
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

	private String t;

	public Message convertWithRoom(Room room) {
		return Message.builder() //
				.id(_id) //
				.room(room) //
				.message(msg) //
				.timestamp(ts != null ? Instant.parse(ts) : null) //
				.user(u.convert()) //
				.attachments(RawAttachment.convertList(attachments)) //
				.type(MessageType.parse(t)) //
				.build();
	}

	public static RawMessage of(Message m) {
		return RawMessage.builder() //
				._id(m.getId()) //
				.rid(m.getRoom().getId()) //
				.msg(m.getMessage()) //
				.ts(m.getTimestamp() != null ? m.getTimestamp().toString() : null) //
				.u(m.getUser() != null ? RawUser.of(m.getUser()) : null) //
				.attachments(RawAttachment.of(m.getAttachments())) //
				.build();
	}
}