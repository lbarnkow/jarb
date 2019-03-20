package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import java.util.UUID;

public class BaseMessageWithMethod extends BaseMessage {
	private static final String MSG = "method";

	private String method;

	BaseMessageWithMethod(String id, String method) {
		super(MSG, id);
		this.method = method;
	}

	public BaseMessageWithMethod(String method) {
		this(UUID.randomUUID().toString(), method);
	}

	public String getMethod() {
		return method;
	}
}
