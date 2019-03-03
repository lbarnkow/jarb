package io.github.lbarnkow.rocketbot.rocketchat.realtime;

public interface WebsocketClientListener {
	void onWebsocketClose(boolean initiatedByClient);

	void onWebsocketMessage(String message);
}
