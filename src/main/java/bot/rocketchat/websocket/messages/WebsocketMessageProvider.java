package bot.rocketchat.websocket.messages;

import javax.inject.Inject;
import javax.inject.Provider;

import bot.rocketchat.websocket.messages.out.SendConnect;
import bot.rocketchat.websocket.messages.out.SendJoinRoom;
import bot.rocketchat.websocket.messages.out.SendLogin;
import bot.rocketchat.websocket.messages.out.SendPong;
import bot.rocketchat.websocket.messages.out.SendStreamRoomMessages;

public class WebsocketMessageProvider {
	@Inject
	private Provider<SendConnect> sendConnectProvider;
	@Inject
	private Provider<SendLogin> sendLoginProvider;
	@Inject
	private Provider<SendStreamRoomMessages> sendStreamRoomMessagesProvider;
	@Inject
	private Provider<SendPong> sendPongProvider;
	@Inject
	private Provider<SendJoinRoom> sendJoinRoomProvider;

	WebsocketMessageProvider() {
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		if (clazz == SendConnect.class)
			return (T) sendConnectProvider.get();
		if (clazz == SendLogin.class)
			return (T) sendLoginProvider.get();
		if (clazz == SendStreamRoomMessages.class)
			return (T) sendStreamRoomMessagesProvider.get();
		if (clazz == SendPong.class)
			return (T) sendPongProvider.get();
		if (clazz == SendJoinRoom.class)
			return (T) sendJoinRoomProvider.get();

		throw new RuntimeException("Can't instantiate unhandled message of type '" + clazz.getSimpleName() + "'!");
	}
}
