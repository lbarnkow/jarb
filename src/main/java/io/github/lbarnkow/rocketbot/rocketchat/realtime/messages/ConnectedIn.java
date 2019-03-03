package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import bot.rocketchat.websocket.messages.Base;

public class ConnectedIn extends Base {
	private String session;

	ConnectedIn() {
	}

	public String getSession() {
		return session;
	}
}
