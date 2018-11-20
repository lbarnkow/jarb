package bot.rocketchat.rest.responses;

import java.util.List;

import bot.CommonBase;
import bot.rocketchat.rest.entities.Room;

public class ChannelListResponse extends CommonBase {
	private List<Room> channels;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	ChannelListResponse() {
	}

	public List<Room> getChannels() {
		return channels;
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
