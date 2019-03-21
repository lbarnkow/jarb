package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;

public class ChatHistoryReply extends BaseReply {

	private List<RawMessage> messages;

	public List<RawMessage> getMessages() {
		return messages;
	}
}
