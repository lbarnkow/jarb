package io.github.lbarnkow.jarb.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Room {
	private final String id;
	private final String name;
	private final RoomType type;
}
