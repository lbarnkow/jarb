package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import lombok.Data;

@MyJsonSettings
@Data
public class RawChannel {
	private String _id;
	private String name;
	private String t;

	public Room asRoom() {
		RoomType type = RoomType.parse(t);
		return Room.builder().id(_id).name(name).type(type).build();
	}

	public static RawChannel of(Room room) {
		RawChannel channel = new RawChannel();
		channel._id = room.getId();
		channel.name = room.getName();
		channel.t = room.getType().getRawType();
		return channel;
	}
}