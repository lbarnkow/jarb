package bot.rocketchat.rest.responses;

import bot.CommonBase;
import bot.rocketchat.rest.Subscription;

public class SubscriptionsGetOneResponse extends CommonBase {
	private Subscription subscription;
	private boolean success;

	public Subscription getSubscription() {
		return subscription;
	}

	public boolean isSuccessful() {
		return success;
	}
}
