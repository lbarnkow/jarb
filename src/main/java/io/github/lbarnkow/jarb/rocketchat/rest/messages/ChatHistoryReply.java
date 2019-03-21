package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryReply extends BaseReply {
	private List<RawMessage> messages;
}
