package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveStreamRoomMessagesSubscriptionUpdate extends BaseMessage {
	private Fields fields;

	@MyJsonSettings
	@Data
	public static class Fields {
		private List<Arg> args;
	}

	@MyJsonSettings
	@Data
	public static class Arg {
		private String rid;
	}
}
