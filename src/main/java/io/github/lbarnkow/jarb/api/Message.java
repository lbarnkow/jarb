package io.github.lbarnkow.jarb.api;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
	private final MessageType type;
	private final Room room;
	private final User user;
	private final String id;
	private final String message;
	private final Instant timestamp;
}
