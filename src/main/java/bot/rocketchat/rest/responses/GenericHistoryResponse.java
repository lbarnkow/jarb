package bot.rocketchat.rest.responses;

import java.util.List;

public class GenericHistoryResponse {
	private List<HistoryMessage> messages;

	public List<HistoryMessage> getMessages() {
		return messages;
	}

	public static class HistoryMessage {
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
	}
}
