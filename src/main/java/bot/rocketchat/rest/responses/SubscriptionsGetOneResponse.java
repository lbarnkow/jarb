package bot.rocketchat.rest.responses;

import bot.rocketchat.Subscription;

public class SubscriptionsGetOneResponse {
	private Subscription subscription;
	private boolean success;

	public Subscription getSubscription() {
		return subscription;
	}

	public boolean isSuccessful() {
		return success;
	}
}
