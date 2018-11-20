package bot.rocketchat.websocket.messages.out;

import bot.rocketchat.websocket.messages.Base;

public class SendPong extends Base {
	SendPong() {
	}

	public SendPong initialize() {
		super.initialize("pong");
		return this;
	}
}
