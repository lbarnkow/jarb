package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.github.lbarnkow.jarb.misc.Common;

public class ReceiveGetSubscriptionsReply extends BaseMessage {

	private List<Subscription> result;

	// Contents will be deserialized from JSON.
	private ReceiveGetSubscriptionsReply() {
		super(null, null);
	}

	public List<Subscription> getResult() {
		return result;
	}

	public static class Subscription extends Common {
		private String t; // "t": "d"
		private Date ts; // "ts": { "$date": 1480377601 }
		private Date ls; // "ls": { "$date": 1480377601 }
		private String name; // "name": "username"
		private String rid; // "rid": "room-id"
		private User u; // "u": { "_id": "user-id", "username": "username" }
		private boolean open; // "open": true
		private boolean alert; // "alert": false
		private int unread; // "unread": 0
		private Date updatedAt; // "_updatedAt": { "$date": 1480377601 }
		private String id; // "_id": "subscription-id"

		public String getT() {
			return t;
		}

		public Date getTs() {
			return ts;
		}

		public Date getLs() {
			return ls;
		}

		public String getName() {
			return name;
		}

		public String getRid() {
			return rid;
		}

		public User getU() {
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

		public Date getUpdatedAt() {
			return updatedAt;
		}

		@JsonAlias("_id")
		public String getId() {
			return id;
		}
	}

	public static class Date extends Common {
		private long date;

		@JsonAlias("$date")
		public long getDate() {
			return date;
		}
	}

	public static class User extends Common {
		private String id;
		private String username;

		@JsonAlias("_id")
		public String getId() {
			return id;
		}

		public String getUsername() {
			return username;
		}
	}
}
