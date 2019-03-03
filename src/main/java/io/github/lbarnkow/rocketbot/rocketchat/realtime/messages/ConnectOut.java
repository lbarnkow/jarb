package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

public class ConnectOut extends Base {

	private String version = "1";
	private String[] support = new String[] { "1" };

	public ConnectOut() {
		super("connect");
	}

	public String getVersion() {
		return version;
	}

	public String[] getSupport() {
		return support;
	}
}
