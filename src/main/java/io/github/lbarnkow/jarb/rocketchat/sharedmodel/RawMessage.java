package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.util.Collections;
import java.util.List;

import lombok.Data;

// see https://rocket.chat/docs/developer-guides/realtime-api/the-message-object/
@MyJsonSettings
@Data
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

	private List<RawAttachment> attachments = Collections.emptyList();

	// alias: (Optional) A way to display the message is “sent” from someone else
	// other than the user who sent the message

	private String avatar;
	private boolean groupable;
	private boolean parseUrls;

	private String t;
}