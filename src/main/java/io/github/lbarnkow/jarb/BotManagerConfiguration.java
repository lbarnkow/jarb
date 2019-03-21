package io.github.lbarnkow.jarb;

import io.github.lbarnkow.jarb.election.ElectionConfiguration;
import io.github.lbarnkow.jarb.rocketchat.ConnectionConfiguration;
import lombok.Data;

@Data
public class BotManagerConfiguration {
	private ElectionConfiguration election = new ElectionConfiguration();
	private ConnectionConfiguration connection = new ConnectionConfiguration();
}
