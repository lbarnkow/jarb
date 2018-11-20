package bot.rocketchat.rest.entities;

import bot.CommonBase;

public class Room extends CommonBase {
	private String _id;
	private String t;

	Room() {
	}

	public Room parse(Subscription sub) {
		this._id = sub.getRoomId();
		this.t = sub.getRoomType().getRocketChatString();
		return this;
	}

	public String getId() {
		return _id;
	}

	public RoomType getType() {
		return RoomType.parse(t);
	}
}