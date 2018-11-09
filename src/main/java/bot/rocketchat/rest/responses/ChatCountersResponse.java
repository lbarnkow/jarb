package bot.rocketchat.rest.responses;

import bot.CommonBase;

public class ChatCountersResponse extends CommonBase {
	private boolean joined;
	private int members;
	private int unreads;
	private String unreadsFrom;
	private int msgs;
	private String latest;
	private int userMentions;
	private boolean success;

	public boolean isJoined() {
		return joined;
	}

	public int getMembers() {
		return members;
	}

	public int getUnreads() {
		return unreads;
	}

	public String getUnreadsFrom() {
		return unreadsFrom;
	}

	public int getMsgs() {
		return msgs;
	}

	public String getLatest() {
		return latest;
	}

	public int getUserMentions() {
		return userMentions;
	}

	public boolean isSuccess() {
		return success;
	}
}
