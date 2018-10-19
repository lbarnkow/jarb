package bot;

public class RocketChatClient {
	private final ConnectionInfo conInfo;
	private final MessageHandler handler;

	public RocketChatClient(ConnectionInfo conInfo, MessageHandler handler) {
		this.conInfo = conInfo;
		this.handler = handler;
	}

	public void start() {
		// TODO
		// connect WebSocket
		// send connect - wait for connected (no unique id!) -> sessionid acquired
		// start login-thread, wait for logged in.

		// start room / subscription thread, wait for first result
		//// refresh public-rooms
		//// get subscriptions (public, private, direct)
		//// join new rooms

		// catch-up
		//// check for unread messages in all subscriptions
		//// handle unread messages and mark as read

		// start real-time subscriptions
	}

	public void stop() {
		// TODO
		// stop subscriptions
		// stop login-thread (+ logout?)
		// close websocket
	}
}