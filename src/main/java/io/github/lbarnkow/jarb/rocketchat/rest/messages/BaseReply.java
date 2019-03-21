package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;

@MyJsonSettings
@Data
public class BaseReply {
	private boolean success;
}
