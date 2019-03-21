package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendConnect extends BaseMessage {
	private String version = "1";
	private String[] support = new String[] { "1" };

	public SendConnect() {
		super("connect");
	}
}
