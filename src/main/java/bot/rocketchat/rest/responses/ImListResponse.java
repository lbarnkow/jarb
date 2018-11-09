package bot.rocketchat.rest.responses;

import java.util.List;

import bot.CommonBase;
import bot.rocketchat.rest.Room;

public class ImListResponse extends CommonBase {
	private List<Room> ims;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	public List<Room> getIms() {
		return ims;
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
