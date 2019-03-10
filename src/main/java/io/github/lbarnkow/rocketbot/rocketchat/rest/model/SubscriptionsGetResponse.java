package io.github.lbarnkow.rocketbot.rocketchat.rest.model;

import java.time.Instant;
import java.util.List;

import io.github.lbarnkow.rocketbot.api.RoomType;
import io.github.lbarnkow.rocketbot.misc.Common;

public class SubscriptionsGetResponse extends Common {

	private List<Subscription> update;
	private List<Subscription> remove;
	private boolean success;

	public List<Subscription> getUpdated() {
		return update;
	}

	public List<Subscription> getRemoved() {
		return remove;
	}

	public boolean isSuccess() {
		return success;
	}

	public static class Subscription extends Common {
		private String t; // "t": "c",
		private String ts; // "ts": "2017-11-25T15:08:17.249Z",
		private String name; // "name": "general",
		private String fname; // "fname": null,
		private String rid; // "rid": "GENERAL",
		private User u;
		private boolean open; // "open": true,
		private boolean alert; // "alert": true,
		private int unread; // "unread": 1,
		private int userMentions; // "userMentions": 1,
		private int groupMentions; // "groupMentions": 0,
		private String _updatedAt; // "_updatedAt": "2017-11-25T15:08:17.249Z",
		private String _id; // "_id": "5ALsG3QhpJfdMpyc8"

		public RoomType getType() {
			return RoomType.parse(t);
		}

		public Instant getTimestamp() {
			return Instant.parse(ts);
		}

		public String getName() {
			return name;
		}

		public String getFname() {
			return fname;
		}

		public String getRoomId() {
			return rid;
		}

		public User getUser() {
			return u;
		}

		public boolean isOpen() {
			return open;
		}

		public boolean isAlert() {
			return alert;
		}

		public int getUnread() {
			return unread;
		}

		public int getUserMentions() {
			return userMentions;
		}

		public int getGroupMentions() {
			return groupMentions;
		}

		public Instant getUpdatedAt() {
			return Instant.parse(_updatedAt);
		}

		public String getId() {
			return _id;
		}

	}

	public static class User extends Common {
		private String _id; // "_id": "EoyAmF4mxx5HxJHJB",
		private String username; // "username": "rocket.cat",
		private String name; // "name": "Rocket Cat"

		public String getId() {
			return _id;
		}

		public String getUsername() {
			return username;
		}

		public String getName() {
			return name;
		}
	}
}
