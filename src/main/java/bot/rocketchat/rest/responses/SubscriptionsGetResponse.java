package bot.rocketchat.rest.responses;

import java.util.List;

import bot.rocketchat.util.RoomType;

public class SubscriptionsGetResponse {
	private List<Subscription> update;
	private List<Subscription> remove;
	private boolean success;

	public List<Subscription> getUpdated() {
		return update;
	}

	public List<Subscription> getRemoved() {
		return remove;
	}

	public boolean isSuccessful() {
		return success;
	}

	public static class Subscription {
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
}
