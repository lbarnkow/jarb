package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendStreamNotifyUser extends BaseSubscription {
	private static final String NAME = "stream-notify-user";

	private final Object[] params;

	public SendStreamNotifyUser(AuthInfo authInfo) {
		super(NAME);

		String param1 = authInfo.getUserId() + "/" + Event.SUBSCRIPTIONS_CHANGED.rawEvent;
		params = new Object[] { param1, false };
	}

	public static enum Event {
		SUBSCRIPTIONS_CHANGED("subscriptions-changed");

		private final String rawEvent;

		private Event(String rawEvent) {
			this.rawEvent = rawEvent;
		}
	}
}
