package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

public class SendGetSubscriptions extends BaseMessageWithMethod {
	private static final String METHOD = "subscriptions/get";

	public SendGetSubscriptions() {
		super(METHOD);
	}
}
