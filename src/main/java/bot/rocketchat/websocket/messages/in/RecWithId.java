package bot.rocketchat.websocket.messages.in;

import bot.rocketchat.websocket.messages.Base;

public class RecWithId extends Base {
	private String id;

	RecWithId() {
	}

	public String getId() {
		return id;
	}
}
