package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatCountersReply extends BaseReply {
	private boolean joined;
	private int unreads;
	private String unreadsFrom;
	private String latest;
}
