package bot.rocketchat;

public enum MessageType {
	CHAT(""), USER_JOIN("uj"), USER_LEAVE("ul"), ADDED_USER("au"), REMOVE_USER("ru"), ROOM_CHANGED_NAME("r"),
	ROOM_CHANGED_PRIVACY("room_changed_privacy"), ROOM_CHANGED_TOPIC("room_changed_topic"),
	ROOM_CHANGED_ANNOUNCEMENT("room_changed_announcement"), ROOM_CHANGED_DESCRIPTION("room_changed_description"),
	UNKNOWN(null);

	private final String t;

	private MessageType(String t) {
		this.t = t;
	}

	public String getRawType() {
		return t;
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