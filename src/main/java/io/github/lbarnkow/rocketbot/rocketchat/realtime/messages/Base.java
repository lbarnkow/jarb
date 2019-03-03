package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.lbarnkow.rocketbot.misc.Common;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Base extends Common {

	private String msg;

	Base() {
	}

	Base(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}
