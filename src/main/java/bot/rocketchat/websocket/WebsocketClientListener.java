package bot.rocketchat.websocket;

public interface WebsocketClientListener {
	void onWebsocketClose(boolean initiatedByClient);
	void onWebsocketMessage(String message);
}