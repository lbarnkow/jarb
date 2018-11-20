package bot.rocketchat.websocket.messages;

import com.google.gson.Gson;

import bot.rocketchat.websocket.RealTimeMessageTypes;

public class Base {
	private static final Gson gson = new Gson();

	private String msg;

	protected Base() {
	}

	protected Base initialize(String msg) {
		this.msg = msg;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public String toString() {
		return gson.toJson(this);
	}

	public boolean is(RealTimeMessageTypes type) {
		if (msg == null)
			return false;
		else
			return msg.equals(type.getText());
	}
}
