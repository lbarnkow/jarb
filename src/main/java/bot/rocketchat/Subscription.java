package bot.rocketchat;

public class Subscription {
	private String _id;
	private String rid;
	private String t;
	private int unread;

	public String getSubId() {
		return _id;
	}

	public String getRoomId() {
		return rid;
	}

	public RoomType getRoomType() {
		return RoomType.parse(t);
	}

	public int getUnread() {
		return unread;
	}
}