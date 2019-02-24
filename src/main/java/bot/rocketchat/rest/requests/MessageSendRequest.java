package bot.rocketchat.rest.requests;

import java.util.ArrayList;
import java.util.List;

import bot.CommonBase;

public class MessageSendRequest extends CommonBase {
	private String roomId;
	private String text;
	private List<Attachment> attachments;

	MessageSendRequest() {
	}

	public MessageSendRequest initialize(String roomId, String text, Attachment... attachments) {
		this.roomId = roomId;
		this.text = text;
		this.attachments = new ArrayList<>();
		if (attachments != null && attachments.length > 0) {
			for (Attachment a : attachments) {
				this.attachments.add(a);
			}
		}
		return this;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getText() {
		return text;
	}

	public List<Attachment> getAttachments() {
		return new ArrayList<>(attachments);
	}

	public static class Attachment {
		private final String title;
		private final String title_link;
		private final String text;
		private final String thumb_url;

		public Attachment(String title, String titleLink, String text, String thumb_url) {
			this.title = title;
			this.title_link = titleLink;
			this.text = text;
			this.thumb_url = thumb_url;
		}

		public String getTitle() {
			return title;
		}

		public String getTitleLink() {
			return title_link;
		}

		public String getText() {
			return text;
		}

		public String getThumb_url() {
			return thumb_url;
		}
	}
}
