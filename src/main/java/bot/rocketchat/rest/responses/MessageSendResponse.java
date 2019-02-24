package bot.rocketchat.rest.responses;

import bot.CommonBase;

public class MessageSendResponse extends CommonBase {
	private boolean success;
	private String channel;

	MessageSendResponse() {
	}

	public boolean isSuccessful() {
		return success;
	}

	public String getChannel() {
		return channel;
	}
}
