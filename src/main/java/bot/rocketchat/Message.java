package bot.rocketchat;

import java.time.Instant;

import bot.CommonBase;

public class Message extends CommonBase {
	private boolean initialized;

	private String id;
	private String text;
	private String roomId;
	private Instant timestamp;
	@SuppressWarnings("unused")
	private String rawType;
	private MessageType type;

	public synchronized void initialize(String id, String text, String roomId, Instant timestamp, MessageType type) {
		if (this.initialized)
			throw new IllegalStateException("Message already initialized!");

		this.id = id;
		this.text = text;
		this.roomId = roomId;
		this.timestamp = timestamp;
		this.rawType = type.getRawType();
		this.type = type;
		this.initialized = true;
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
}