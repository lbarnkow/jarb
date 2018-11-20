package bot.rocketchat.rest.responses;

import bot.CommonBase;
import bot.rocketchat.rest.entities.Subscription;

public class SubscriptionsGetOneResponse extends CommonBase {
	private Subscription subscription;
	private boolean success;

	SubscriptionsGetOneResponse() {
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public boolean isSuccessful() {
		return success;
	}
}
