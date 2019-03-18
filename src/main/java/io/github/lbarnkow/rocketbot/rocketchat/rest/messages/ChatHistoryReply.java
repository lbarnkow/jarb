package io.github.lbarnkow.rocketbot.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.rocketbot.misc.Common;

public class ChatHistoryReply extends BaseReply {

	private List<Message> messages;

	public List<Message> getMessages() {
		return messages;
	}

	public static class Message extends Common {
		private String _id;
		private String t;
		private String rid;
		private String msg;
		private String ts;
		private User u;

		public String get_id() {
			return _id;
		}

		public String getT() {
			return t;
		}

		public String getRid() {
			return rid;
		}

		public String getMsg() {
			return msg;
		}

		public String getTs() {
			return ts;
		}

		public User getU() {
			return u;
		}
	}

	public static class User extends Common {
		private String _id;
		private String username;

		public String get_id() {
			return _id;
		}

		public String getUsername() {
			return username;
		}
	}
}
