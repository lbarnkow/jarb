package io.github.lbarnkow.rocketbot.api;

public interface Bot {
	void initialize();

	void getCredentials();

	void getName();

	boolean shouldAutojoinPublicChannels();

	boolean joinChannel(Channel channel);

	void handleMessage(Channel channel, Message message);
}
