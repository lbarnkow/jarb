package io.github.lbarnkow.jarb.rocketchat.realtime;

public interface WebsocketClientListener {
  void onWebsocketClose(boolean initiatedByClient);

  void onWebsocketMessage(String message);
}
