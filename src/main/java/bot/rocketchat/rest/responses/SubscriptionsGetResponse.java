package bot.rocketchat.rest.responses;

import java.util.List;

import bot.rocketchat.Subscription;

public class SubscriptionsGetResponse {
	private List<Subscription> update;
	private boolean success;

	public List<Subscription> getUpdated() {
		return update;
	}

	public boolean isSuccessful() {
		return success;
	}
}
