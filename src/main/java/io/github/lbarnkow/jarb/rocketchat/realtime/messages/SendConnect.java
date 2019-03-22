package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendConnect extends BaseMessage {
	private String version = "1";
	private String[] support = new String[] { "1" };

	public SendConnect() {
		super("connect");
	}
}
