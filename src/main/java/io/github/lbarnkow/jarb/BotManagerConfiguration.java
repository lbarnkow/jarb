package io.github.lbarnkow.jarb;

import io.github.lbarnkow.jarb.api.BotConfiguration;
import io.github.lbarnkow.jarb.election.ElectionConfiguration;
import io.github.lbarnkow.jarb.rocketchat.ConnectionConfiguration;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@JarbJsonSettings
@Data
public class BotManagerConfiguration {
  private ElectionConfiguration election = new ElectionConfiguration();
  private ConnectionConfiguration connection = new ConnectionConfiguration();
  private List<BotConfiguration> bots = Arrays.asList(new BotConfiguration());
}
