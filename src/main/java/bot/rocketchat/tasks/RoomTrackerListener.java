package bot.rocketchat.tasks;

import java.util.List;

import bot.rocketchat.rest.entities.Room;

public interface RoomTrackerListener {
	void onNewRooms(List<Room> newRooms);
}