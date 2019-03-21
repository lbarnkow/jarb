package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;

@MyJsonSettings
@Data
public class ReceiveConnected {
	private String session;
}
