package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import java.util.List;

import io.github.lbarnkow.rocketbot.misc.Common;

public class ReceiveStreamRoomMessagesSubscriptionUpdate extends BaseMessage {

	private Fields fields;

	// Contents will be deserialized from JSON.
	private ReceiveStreamRoomMessagesSubscriptionUpdate() {
		super(null, null);
	}

	public Fields getFields() {
		return fields;
	}

	public static class Fields extends Common {
		private List<Arg> args;

		public List<Arg> getArgs() {
			return args;
		}
	}

	public static class Arg extends Common {
		private String rid;

		public String getRid() {
			return rid;
		}
	}
}
