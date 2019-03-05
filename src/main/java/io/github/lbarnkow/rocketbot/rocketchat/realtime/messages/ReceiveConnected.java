package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import bot.rocketchat.websocket.messages.Base;

public class ReceiveConnected extends Base {
	private String session;

	ReceiveConnected() {
	}

	public String getSession() {
		return session;
	}
}
