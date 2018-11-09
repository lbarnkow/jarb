package bot.rocketchat.rest.responses;

import java.util.List;

import bot.CommonBase;
import bot.rocketchat.rest.Subscription;

public class SubscriptionsGetResponse extends CommonBase {
	private List<Subscription> update;
	private boolean success;

	public List<Subscription> getUpdated() {
		return update;
	}

	public boolean isSuccessful() {
		return success;
	}
}
