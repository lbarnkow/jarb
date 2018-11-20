package bot.rocketchat.websocket.messages.in;

import bot.rocketchat.websocket.messages.Base;

public class RecConnected extends Base {
	private String session;

	RecConnected() {
	}

	public String getSession() {
		return session;
	}
}
