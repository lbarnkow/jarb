package bot.rocketchat.rest.responses;

import java.util.List;

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
		// private String t;
		// private String ts;
		// private String name;
		// private String fname;
		// private String rid;
		// "u": {
		// "_id": "EoyAmF4mxx5HxJHJB",
		// "username": "rocket.cat",
		// "name": "Rocket Cat"
		// },
		// "open": true,
		// "alert": true,
		// "unread": 1,
		// "userMentions": 1,
		// "groupMentions": 0,
		// "_updatedAt": "2017-11-25T15:08:17.249Z",

		public String getId() {
			return _id;
		}
	}
}
