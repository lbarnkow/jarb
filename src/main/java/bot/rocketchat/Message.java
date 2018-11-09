package bot.rocketchat;

import bot.CommonBase;

public final class Message extends CommonBase {
	private final String id;
	private final String text;
	private final String roomId;

	public Message(String id, String text, String roomId) {
		this.id = id;
		this.text = text;
		this.roomId = roomId;
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
}