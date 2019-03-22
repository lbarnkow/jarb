package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendGetSubscriptions extends BaseMessageWithMethod {
	private static final String METHOD = "subscriptions/get";

	public SendGetSubscriptions() {
		super(METHOD);
	}
}
