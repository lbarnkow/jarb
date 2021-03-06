/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb;

import io.github.lbarnkow.jarb.api.BotConfiguration;
import io.github.lbarnkow.jarb.election.ElectionConfiguration;
import io.github.lbarnkow.jarb.rocketchat.ConnectionConfiguration;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

/**
 * POJO representation of the main configuration file.
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
public class BotManagerConfiguration {
  /**
   * Subsection for election parameters.
   */
  private ElectionConfiguration election = new ElectionConfiguration();

  /**
   * Subsection for chat server connection parameters.
   */
  private ConnectionConfiguration connection = new ConnectionConfiguration();

  /**
   * Subsection for bot configurations.
   */
  private List<BotConfiguration> bots = Arrays.asList(new BotConfiguration());
}
