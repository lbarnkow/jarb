package bot.rocketchat.rest.requests;

import bot.CommonBase;

public class SubscriptionsReadRequest extends CommonBase {
	private String rid;

	SubscriptionsReadRequest() {
	}

	public SubscriptionsReadRequest initialize(String roomId) {
		rid = roomId;
		return this;
	}

	public String getRoomId() {
		return rid;
	}
}
