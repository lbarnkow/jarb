package bot.rocketchat;

import java.time.Instant;

import bot.CommonBase;

public final class Message extends CommonBase {
	private final String id;
	private final String text;
	private final String roomId;
	private final Instant timestamp;
	@SuppressWarnings("unused")
	private final String rawType;
	private final MessageType type;

	public Message(String id, String text, String roomId, Instant timestamp, String type) {
		this.id = id;
		this.text = text;
		this.roomId = roomId;
		this.timestamp = timestamp;
		this.rawType = type;
		this.type = MessageType.parse(type);
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getRoomId() {
		return roomId;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public MessageType getType() {
		return type;
	}

	public enum MessageType {
		CHAT(""), USER_JOIN("uj"), USER_LEAVE("ul"), ADDED_USER("au"), REMOVE_USER("ru"), ROOM_CHANGED_NAME("r"),
		ROOM_CHANGED_PRIVACY("room_changed_privacy"), ROOM_CHANGED_TOPIC("room_changed_topic"),
		ROOM_CHANGED_ANNOUNCEMENT("room_changed_announcement"), ROOM_CHANGED_DESCRIPTION("room_changed_description"),
		UNKNOWN(null);

		private final String t;

		private MessageType(String t) {
			this.t = t;
		}

		public static MessageType parse(String t) {
			if (t == null)
				return UNKNOWN;

			for (MessageType type : MessageType.values())
				if (t.equals(type.t))
					return type;

			return UNKNOWN;
		}
	}
}