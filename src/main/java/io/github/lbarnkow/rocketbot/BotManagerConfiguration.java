package io.github.lbarnkow.rocketbot;

import io.github.lbarnkow.rocketbot.election.ElectionConfiguration;
import io.github.lbarnkow.rocketbot.rocketchat.ConnectionConfiguration;

public class BotManagerConfiguration {
	private ElectionConfiguration election = new ElectionConfiguration();
	private ConnectionConfiguration connection = new ConnectionConfiguration();

	public ElectionConfiguration getElection() {
		return election;
	}

	public ConnectionConfiguration getConnection() {
		return connection;
	}
}
