package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

public class SendGetSubscriptions extends BaseMessageWithMethod {
	private static final String METHOD = "subscriptions/get";

	public SendGetSubscriptions() {
		super(METHOD);
	}
}
