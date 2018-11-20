package bot.rocketchat.websocket.messages.out;

import java.util.UUID;

import bot.rocketchat.websocket.messages.Base;

public class SendWithId extends Base {
	@SuppressWarnings("unused")
	private String id;

	SendWithId() {
	}

	private SendWithId _initialize(String id, String msg) {
		super.initialize(msg);
		if (id == null)
			this.id = UUID.randomUUID().toString();
		else
			this.id = id;
		return this;
	}

	public SendWithId initialize(String id, String msg) {
		return _initialize(id, msg);
	}

	public SendWithId initialize(String msg) {
		return _initialize(null, msg);
	}
}
