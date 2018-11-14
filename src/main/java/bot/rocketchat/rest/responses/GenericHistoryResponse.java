package bot.rocketchat.rest.responses;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bot.CommonBase;
import bot.rocketchat.Message;

public class GenericHistoryResponse extends CommonBase {
	private List<HistoryMessage> messages;

	public List<HistoryMessage> getMessages() {
		Collections.sort(messages, new Comparator<HistoryMessage>() {
			@Override
			public int compare(HistoryMessage o1, HistoryMessage o2) {
				return o1.ts.compareTo(o2.ts);
			}
		});

		return messages;
	}

	public static class HistoryMessage extends CommonBase {
		private String _id;
		private String rid;
		private String msg;
		private String ts;
		private String t = "";

		public String get_id() {
			return _id;
		}

		public String getRid() {
			return rid;
		}

		public String getMsg() {
			return msg;
		}

		public String getTimeStamp() {
			return ts;
		}

		public String getType() {
			return t;
		}

		public Message toMessage() {
			Instant timestamp = Instant.parse(ts);
			return new Message(_id, msg, rid, timestamp, t);
		}
	}
}
