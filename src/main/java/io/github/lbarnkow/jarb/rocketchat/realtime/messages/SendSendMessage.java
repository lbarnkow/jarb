package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.Arrays;
import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSendMessage extends BaseMessageWithMethod {
	private static final String METHOD = "sendMessage";

	private List<RawMessage> params;

	public SendSendMessage(RawMessage message) {
		super(METHOD);
		params = Arrays.asList(message);
	}
}
