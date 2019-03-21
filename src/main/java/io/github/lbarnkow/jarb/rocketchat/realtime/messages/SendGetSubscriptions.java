package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendGetSubscriptions extends BaseMessageWithMethod {
	private static final String METHOD = "subscriptions/get";

	public SendGetSubscriptions() {
		super(METHOD);
	}
}
