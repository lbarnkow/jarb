package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import io.github.lbarnkow.rocketbot.api.Bot;

public class SendStreamNotifyUser extends BaseSubscription {
	private static final String NAME = "stream-notify-user";

	private final Object[] params;

	public SendStreamNotifyUser(Bot bot) {
		super(NAME);

		String param1 = bot.getAuthHolder().get().getUserId() + "/" + Event.SUBSCRIPTIONS_CHANGED.rawEvent;
		params = new Object[] { param1, false };
	}

	public Object[] getParams() {
		return params;
	}

	public static enum Event {
		SUBSCRIPTIONS_CHANGED("subscriptions-changed");

		private final String rawEvent;

		private Event(String rawEvent) {
			this.rawEvent = rawEvent;
		}
	}
}
