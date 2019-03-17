package io.github.lbarnkow.rocketbot.rocketchat.rest.messages;

public class ChatCountersReply extends BaseReply {
	private int unreads;
	private String unreadsFrom;
	private String latest;

	public int getUnreads() {
		return unreads;
	}

	public String getUnreadsFrom() {
		return unreadsFrom;
	}

	public String getLatest() {
		return latest;
	}
}
