package io.github.lbarnkow.rocketbot.api;

import io.github.lbarnkow.rocketbot.misc.Common;

public class User extends Common {
	private final String id;
	private final String name;

	public User(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
