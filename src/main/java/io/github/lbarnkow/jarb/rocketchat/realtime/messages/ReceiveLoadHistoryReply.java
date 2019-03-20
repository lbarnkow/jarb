package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

//public class ReceiveLoadHistoryReply extends BaseMessage {
//
//	private Result result;
//
//	// Contents will be deserialized from JSON.
//	private ReceiveLoadHistoryReply() {
//		super(null, null);
//	}
//
//	public Result getResult() {
//		return result;
//	}
//
//	public static class Result extends Common {
//		private List<Message> messages;
//		private int unreadNotLoaded;
//
//		public List<Message> getMessages() {
//			return messages;
//		}
//
//		public int getUnreadNotLoaded() {
//			return unreadNotLoaded;
//		}
//	}
//
//	public static class Message extends Common {
//		private String _id;
//		private String msg;
//		private User u;
//		private Date ts;
//
//		public String get_id() {
//			return _id;
//		}
//
//		public String getMsg() {
//			return msg;
//		}
//
//		public User getU() {
//			return u;
//		}
//
//		public Date getTs() {
//			return ts;
//		}
//	}
//
//	public static class User extends Common {
//		private String _id;
//		private String username;
//
//		public String get_id() {
//			return _id;
//		}
//
//		public String getUsername() {
//			return username;
//		}
//	}
//}
