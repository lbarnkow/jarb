package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

public class SendConnect extends Base {

	private String version = "1";
	private String[] support = new String[] { "1" };

	public SendConnect() {
		super("connect");
	}

	public String getVersion() {
		return version;
	}

	public String[] getSupport() {
		return support;
	}
}
