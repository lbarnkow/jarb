package io.github.lbarnkow.jarb.rocketchat;

public interface RealtimeClientListener {
  void onRealtimeClientSessionEstablished(RealtimeClient source);

  void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient);

  void onRealtimeClientStreamRoomMessagesUpdate(RealtimeClient source, String roomId);
}
