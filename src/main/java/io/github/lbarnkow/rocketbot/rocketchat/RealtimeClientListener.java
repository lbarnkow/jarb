package io.github.lbarnkow.rocketbot.rocketchat;

public interface RealtimeClientListener {
	void onRealtimeClientSessionEstablished();

	void onRealtimeClientSessionClose(boolean initiatedByClient);
}
