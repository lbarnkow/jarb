package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.UUID;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
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
}
