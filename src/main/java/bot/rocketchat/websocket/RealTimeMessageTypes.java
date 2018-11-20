package bot.rocketchat.websocket;

public enum RealTimeMessageTypes {
	ADDED, CHANGED, CONNECTED, PING, READY, RESULT, UPDATED;

	private final String type;

	private RealTimeMessageTypes() {
		type = name().toLowerCase();
	}

	public String getText() {
		return type;
	}
}
