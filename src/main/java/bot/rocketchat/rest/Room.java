package bot.rocketchat.rest;

import bot.CommonBase;

public class Room extends CommonBase {
	private String _id;
	private String t;

	public Room() {
	}

	public Room(Subscription sub) {
		this._id = sub.getRoomId();
		this.t = sub.getRoomType().getRocketChatString();
	}

	public String getId() {
		return _id;
	}

	public RoomType getType() {
		return RoomType.parse(t);
	}
}