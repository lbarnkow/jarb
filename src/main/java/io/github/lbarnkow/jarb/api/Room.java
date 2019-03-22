package io.github.lbarnkow.jarb.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Room {
	final String id;
	final String name;
	final RoomType type;
}
