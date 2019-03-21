package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.Arrays;
import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendJoinRoom extends BaseMessageWithMethod {
	private static final String METHOD = "joinRoom";

	private final List<String> params;

	public SendJoinRoom(String roomId) {
		super(METHOD);

		this.params = Arrays.asList(roomId);
	}
}
