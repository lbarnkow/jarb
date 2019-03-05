package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import io.github.lbarnkow.rocketbot.misc.Common;

public class Base extends Common {

	private String msg;
	private String id;

	// for deserialization
	@SuppressWarnings("unused")
	private Base() {
	}

	Base(String msg, String id) {
		this.msg = msg;
		this.id = id;
	}

	Base(String msg) {
		this(msg, null);
	}

	public String getMsg() {
		return msg;
	}

	public String getId() {
		return id;
	}
}
