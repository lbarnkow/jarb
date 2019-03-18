package io.github.lbarnkow.rocketbot.api;

import java.time.Instant;

import io.github.lbarnkow.rocketbot.misc.Common;

public class Message extends Common {
	private final MessageType type;
	private final Room room;
	private final User user;
	private final String id;
	private final String message;
	private final Instant timestamp;

	public Message(MessageType type, Room room, User user, String id, String message, Instant timestamp) {
		this.type = type;
		this.room = room;
		this.user = user;
		this.id = id;
		this.message = message;
		this.timestamp = timestamp;
	}

	public MessageType getType() {
		return type;
	}

	public Room getRoom() {
		return room;
	}

	public User getUser() {
		return user;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
}
