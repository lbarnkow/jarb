package bot.rocketchat.websocket.messages.out;

import java.util.ArrayList;
import java.util.List;

public class SendJoinRoom extends SendMethod {
	private static final String METHOD = "joinRoom";

	@SuppressWarnings("unused")
	private String[] params;

	SendJoinRoom() {
	}

	private SendJoinRoom _initialize(String roomId, String joinCode) {
		super.initialize(METHOD);

		List<Object> params = new ArrayList<>();
		params.add(roomId);
		if (joinCode != null)
			params.add(joinCode);
		this.params = params.toArray(new String[0]);
		return this;
	}

	public SendJoinRoom initialize(String roomId, String joinCode) {
		return _initialize(roomId, joinCode);
	}

	public SendJoinRoom initialize(String roomId) {
		return _initialize(roomId, null);
	}
}
