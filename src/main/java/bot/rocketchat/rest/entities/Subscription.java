package bot.rocketchat.rest.entities;

import bot.CommonBase;

public class Subscription extends CommonBase {
	private String _id;
	private String rid;
	private String t;
	private int unread;

	Subscription() {
	}

	public String getSubId() {
		return _id;
	}

	public String getRoomId() {
		return rid;
	}

	public RoomType getRoomType() {
		return RoomType.parse(t);
	}

	public boolean hasUnread() {
		return unread != 0;
	}

	public int getUnread() {
		return unread;
	}
}