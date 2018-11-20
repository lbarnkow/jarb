package bot.rocketchat.rest.responses;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bot.CommonBase;
import bot.rocketchat.MessageType;

public class GenericHistoryResponse extends CommonBase {
	private List<HistoryMessage> messages;

	GenericHistoryResponse() {
	}

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

		HistoryMessage() {
		}

		public String getId() {
			return _id;
		}

		public String getRoomId() {
			return rid;
		}

		public String getText() {
			return msg;
		}

		public Instant getTimeStamp() {
			return Instant.parse(ts);
		}

		public MessageType getType() {
			return MessageType.parse(t);
		}
	}
}
