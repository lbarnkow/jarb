package bot.rocketchat.websocket.messages.in;

import java.util.ArrayList;
import java.util.List;

import bot.rocketchat.util.Tuple;

public class RecChangedStreamRoomMessages extends RecChangedSub {
	public static final String COLLECTION = "stream-room-messages";

	private Fields fields;

	RecChangedStreamRoomMessages() {
	}

	public Fields getFields() {
		return fields;
	}

	public List<Tuple<String, String>> getMessages() {
		List<Tuple<String, String>> messages = new ArrayList<>();

		for (Args arg : fields.getArgs())
			messages.add(new Tuple<>(arg.getRoomId(), arg.getMessage()));

		return messages;
	}

	public static class Fields {
		private String eventName;
		private Args[] args;

		Fields() {
		}

		public String getEventName() {
			return eventName;
		}

		public Args[] getArgs() {
			return args;
		}
	}

	public static class Args {
		private String rid;
		private String msg;

		Args() {
		}

		public String getRoomId() {
			return rid;
		}

		public String getMessage() {
			return msg;
		}
	}
}
