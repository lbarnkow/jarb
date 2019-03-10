package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import java.util.Arrays;
import java.util.List;

public class SendJoinRoom extends BaseMessageWithMethod {
	private static final String METHOD = "joinRoom";

	@SuppressWarnings("unused")
	private final List<String> params;

	public SendJoinRoom(String roomId) {
		super(METHOD);

		this.params = Arrays.asList(roomId);
	}
}
