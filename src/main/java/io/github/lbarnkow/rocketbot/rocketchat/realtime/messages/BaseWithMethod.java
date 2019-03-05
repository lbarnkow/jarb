package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import java.util.UUID;

public class BaseWithMethod extends Base {
	private static final String MSG = "method";

	private String method;

	BaseWithMethod(String id, String method) {
		super(MSG, id);
		this.method = method;
	}

	public BaseWithMethod(String method) {
		this(UUID.randomUUID().toString(), method);
	}

	public String getMethod() {
		return method;
	}
}
