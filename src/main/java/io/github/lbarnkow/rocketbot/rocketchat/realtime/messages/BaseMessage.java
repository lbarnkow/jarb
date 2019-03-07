package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import io.github.lbarnkow.rocketbot.misc.Common;

public class BaseMessage extends Common {

	private String msg;
	private String id;
	private Error error;

	// for deserialization
	@SuppressWarnings("unused")
	private BaseMessage() {
	}

	BaseMessage(String msg, String id) {
		this.msg = msg;
		this.id = id;
	}

	BaseMessage(String msg) {
		this(msg, null);
	}

	public String getMsg() {
		return msg;
	}

	public String getId() {
		return id;
	}

	public Error getError() {
		return error;
	}

	public static class Error extends Common {
		private boolean isClientSafe;
		private int error;
		private String reason;
		private String message;
		private String errorType;

		public boolean isClientSafe() {
			return isClientSafe;
		}

		public int getError() {
			return error;
		}

		public String getReason() {
			return reason;
		}

		public String getMessage() {
			return message;
		}

		public String getErrorType() {
			return errorType;
		}
	}
}
