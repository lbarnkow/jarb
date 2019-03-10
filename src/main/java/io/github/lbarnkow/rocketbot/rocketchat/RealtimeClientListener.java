package io.github.lbarnkow.rocketbot.rocketchat;

public interface RealtimeClientListener {
	void onRealtimeClientSessionEstablished(RealtimeClient source);

	void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient);
}
