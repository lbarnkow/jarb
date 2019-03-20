package io.github.lbarnkow.rocketbot.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.rocketbot.misc.Common;

public class ChannelListReply extends BaseReply {

	private List<Channel> channels;

	public List<Channel> getChannels() {
		return channels;
	}

	public static class Channel extends Common {
		private String _id;
		private String name;
		private String t;

		public String get_id() {
			return _id;
		}

		public String getName() {
			return name;
		}

		public String getT() {
			return t;
		}
	}
}
