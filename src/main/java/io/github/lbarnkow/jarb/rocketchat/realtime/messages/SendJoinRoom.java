package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.ArrayList;
import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendJoinRoom extends BaseMessageWithMethod {
	private static final String METHOD = "joinRoom";

	private final List<String> params = new ArrayList<>();;

	public SendJoinRoom(String roomId, String joinCode) {
		super(METHOD);

		this.params.add(roomId);
		if (joinCode != null) {
			this.params.add(joinCode);
		}
	}

	public SendJoinRoom(String roomId) {
		this(roomId, null);
	}
}
