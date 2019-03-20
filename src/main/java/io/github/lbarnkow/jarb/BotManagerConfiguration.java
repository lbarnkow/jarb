package io.github.lbarnkow.jarb;

import io.github.lbarnkow.jarb.election.ElectionConfiguration;
import io.github.lbarnkow.jarb.rocketchat.ConnectionConfiguration;

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
