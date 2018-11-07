package bot.rocketchat.websocket.messages;

import java.util.ArrayList;
import java.util.List;

import bot.rocketchat.util.Tuple;

public class RecChangedStreamRoomMessages extends RecChangedSub {
	public static final String COLLECTION = "stream-room-messages";

	private Fields fields;

	public Fields getFields() {
		return fields;
	}

	public List<Tuple<String, String>> getMessages() {
		List<Tuple<String, String>> messages = new ArrayList<>();

		for (Args arg : fields.getArgs())
			messages.add(new Tuple<>(arg.getRoomId(), arg.getMessage()));

		return messages;
	}

	public static RecChangedStreamRoomMessages parse(String json) {
		return parse(json, RecChangedStreamRoomMessages.class);
	}

	public static class Fields {
		private String eventName;
		private Args[] args;

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

		public String getRoomId() {
			return rid;
		}

		public String getMessage() {
			return msg;
		}
	}
}
