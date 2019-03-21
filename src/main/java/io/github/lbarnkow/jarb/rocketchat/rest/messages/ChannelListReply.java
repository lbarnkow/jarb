package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;

public class ChannelListReply extends BaseReply {

	private List<RawChannel> channels;

	public List<RawChannel> getChannels() {
		return channels;
	}
}
