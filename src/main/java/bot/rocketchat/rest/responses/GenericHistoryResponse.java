package bot.rocketchat.rest.responses;

import java.util.List;

import bot.CommonBase;
import bot.rocketchat.Message;

public class GenericHistoryResponse extends CommonBase {
	private List<HistoryMessage> messages;

	public List<HistoryMessage> getMessages() {
		return messages;
	}

	public static class HistoryMessage extends CommonBase {
		private String _id;
		private String rid;
		private String msg;

		public String get_id() {
			return _id;
		}

		public String getRid() {
			return rid;
		}

		public String getMsg() {
			return msg;
		}

		public Message asMessage() {
			return new Message(_id, msg, rid);
		}
	}
}
