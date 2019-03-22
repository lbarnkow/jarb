package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.NoArgsConstructor;

@JarbJsonSettings
@Data
@NoArgsConstructor
public class BaseMessage {

	private String msg;
	private String id;
	private String collection;
	private Error error;

	BaseMessage(String msg, String id) {
		this.msg = msg;
		this.id = id;
	}

	BaseMessage(String msg) {
		this(msg, null);
	}

	@JarbJsonSettings
	@Data
	public static class Error {
		private boolean isClientSafe;
		private int error;
		private String reason;
		private String message;
		private String errorType;
	}
}
