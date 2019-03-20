package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import io.github.lbarnkow.rocketbot.api.Room;

public class SendStreamRoomMessages extends BaseSubscription {
	private static final String NAME = "stream-room-messages";
	public static final String COLLECTION = NAME;

	private final Object[] params;

	public SendStreamRoomMessages(Room room) {
		super(NAME);
		params = new Object[] { room.getId(), false };
	}

	public Object[] getParams() {
		return params;
	}
}
