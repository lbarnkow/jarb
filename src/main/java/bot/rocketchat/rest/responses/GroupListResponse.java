package bot.rocketchat.rest.responses;

import java.util.List;

import bot.rocketchat.Room;

public class GroupListResponse {
	private List<Room> groups;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	public List<Room> getGroups() {
		return groups;
	}

	public int getOffset() {
		return offset;
	}

	public int getCount() {
		return count;
	}

	public int getTotal() {
		return total;
	}

	public boolean isSuccessful() {
		return success;
	}
}
