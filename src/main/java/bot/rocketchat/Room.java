package bot.rocketchat;

public class Room {
	private String _id;
	private String t;

	public Room() {
	}

	public Room(Subscription sub) {
		this._id = sub.getRoomId();
		this.t = sub.getRoomType().getRocketChatString();
	}

	public String getRoomId() {
		return _id;
	}

	public RoomType getRoomType() {
		return RoomType.parse(t);
	}
}