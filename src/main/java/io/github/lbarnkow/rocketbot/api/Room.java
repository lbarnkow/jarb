package io.github.lbarnkow.rocketbot.api;

import io.github.lbarnkow.rocketbot.misc.Common;

public class Room extends Common {
	private final String id;
	private final String name;
	private final RoomType type;

	public Room(String id, String name, RoomType type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public RoomType getType() {
		return type;
	}
}
