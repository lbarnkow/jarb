package bot.rocketchat.rest.requests;

import bot.CommonBase;

public class SubscriptionsReadRequest extends CommonBase {
	private final String rid;

	public SubscriptionsReadRequest(String roomId) {
		rid = roomId;
	}

	public String getRoomId() {
		return rid;
	}
}
