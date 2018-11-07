package bot.rocketchat.websocket.messages;

public class SendStreamRoomMessages extends SendSub {
	private static final String NAME = "stream-room-messages";

	@SuppressWarnings("unused")
	private Object[] params;

	public SendStreamRoomMessages(String roomId) {
		super(NAME);
		params = new Object[] { roomId, false };
	}
}
