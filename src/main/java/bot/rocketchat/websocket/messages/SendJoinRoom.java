package bot.rocketchat.websocket.messages;

import java.util.ArrayList;
import java.util.List;

public class SendJoinRoom extends SendMethod {
	private static final String METHOD = "joinRoom";

	@SuppressWarnings("unused")
	private final String[] params;

	public SendJoinRoom(String roomId) {
		this(roomId, null);
	}

	public SendJoinRoom(String roomId, String joinCode) {
		super(METHOD);

		List<Object> params = new ArrayList<>();
		params.add(roomId);
		if (joinCode != null)
			params.add(joinCode);
		this.params = params.toArray(new String[0]);
	}
}
