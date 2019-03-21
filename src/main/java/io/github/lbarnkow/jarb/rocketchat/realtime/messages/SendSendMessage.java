package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.Arrays;
import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawAttachment;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSendMessage extends BaseMessageWithMethod {
	private static final String METHOD = "sendMessage";

	private List<RawMessage> params;

	// TODO: Clean up! Currently only a prototype to do quick tests.
	public SendSendMessage(String rid, String msg, String attachmentText) {
		super(METHOD);

		RawMessage message = new RawMessage();
		message.setRid(rid);
		message.setMsg(msg);
		RawAttachment att = new RawAttachment();
		message.setAttachments(Arrays.asList(att));

		params = Arrays.asList(message);
	}
}
