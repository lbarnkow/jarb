package bot.rocketchat.websocket.messages.out;

import bot.rocketchat.websocket.messages.Base;

public class SendConnect extends Base {
	private String version = "1";
	private String[] support = new String[] { "1" };

	SendConnect() {
	}

	public SendConnect initialize() {
		super.initialize("connect");
		return this;
	}

	public String getVersion() {
		return version;
	}

	public String[] getSupport() {
		return support;
	}
}
