package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

public class SendStreamRoomMessages extends BaseSubscription {
	private static final String NAME = "stream-room-messages";
	public static final String COLLECTION = NAME;

	private final Object[] params;

	public SendStreamRoomMessages(String roomId) {
		super(NAME);
		params = new Object[] { roomId, false };
	}

	public Object[] getParams() {
		return params;
	}
}
