package bot.rocketchat.websocket;

import bot.rocketchat.websocket.messages.Base;

public enum MessageTypes {
	ADDED, CHANGED, CONNECTED, PING, READY, RESULT, UPDATED;

	private final String type;

	private MessageTypes() {
		type = name().toLowerCase();
	}

	public boolean matches(Base message) {
		return type.equals(message.getMsg());
	}
}
