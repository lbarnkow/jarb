package bot.rocketchat.rest.responses;

import java.util.List;

import bot.CommonBase;
import bot.rocketchat.rest.entities.Room;

public class GroupListResponse extends CommonBase {
	private List<Room> groups;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	GroupListResponse() {
	}

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
