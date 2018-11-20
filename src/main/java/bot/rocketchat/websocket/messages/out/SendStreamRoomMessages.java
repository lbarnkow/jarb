package bot.rocketchat.websocket.messages.out;

public class SendStreamRoomMessages extends SendSub {
	private static final String NAME = "stream-room-messages";

	@SuppressWarnings("unused")
	private Object[] params;

	SendStreamRoomMessages() {
	}

	public SendStreamRoomMessages initialize(String roomId) {
		super.initialize(NAME);
		params = new Object[] { roomId, false };
		return this;
	}
}
