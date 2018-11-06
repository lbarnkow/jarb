package bot.rocketchat.websocket;

import bot.rocketchat.websocket.messages.Base;

public enum MessageTypes {
	PING, CONNECTED, RESULT, ADDED, UPDATED;

	private final String type;

	private MessageTypes() {
		type = name().toLowerCase();
	}

	public boolean matches(Base message) {
		return type.equals(message.getMsg());
	}
}
