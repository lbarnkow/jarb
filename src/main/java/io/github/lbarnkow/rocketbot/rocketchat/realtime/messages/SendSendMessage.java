package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import java.util.Arrays;
import java.util.List;

import io.github.lbarnkow.rocketbot.misc.Common;

// TODO: Clean up! Currently only a prototype to do quick tests.
public class SendSendMessage extends BaseMessageWithMethod {
	private static final String METHOD = "sendMessage";

	private List<Message> params;

	public SendSendMessage(String rid, String msg, String attachmentText) {
		super(METHOD);

		params = Arrays.asList(new Message(rid, msg, attachmentText));
	}

	public List<Message> getParams() {
		return params;
	}

	public static class Message extends Common {
		private String rid;
		private String msg;
		private List<Attachment> attachments;

		public Message(String rid, String msg, String attachmentText) {
			this.rid = rid;
			this.msg = msg;
			this.attachments = Arrays.asList(new Attachment(attachmentText));
		}

		public String getRid() {
			return rid;
		}

		public String getMsg() {
			return msg;
		}

		public List<Attachment> getAttachments() {
			return attachments;
		}
	}

	public static class Attachment extends Common {
		private String text;

		public Attachment(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
