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

package io.github.lbarnkow.jarb.api;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.bots.dummy.DummyBot;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Per bot configuration loaded from YAML.
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
public class BotConfiguration {
  /**
   * The default name for a bot, when it is missing from the yaml file.
   */
  private static final String DEFAULT_NAME = "demobot";

  /**
   * The default implementation for a bot, when it is missing from the yaml file.
   */
  private static final String DEF_CLASS_NAME = DummyBot.class.getName();

  /**
   * The default login credentials to the chat server for a bot, when they are
   * missing from the yaml file.
   */
  private static final Credentials DEF_CREDENTIALS =
      new Credentials("demobot", DigestUtils.sha256Hex("demobot"));

  /**
   * The bot's name.
   */
  private String name = DEFAULT_NAME;

  /**
   * The bot's implementation.
   */
  private String className = DEF_CLASS_NAME;

  /**
   * The bot's chat server login credentials.
   */
  private Credentials credentials = DEF_CREDENTIALS;

  /**
   * Custom run-time settings to be interpreted by the bot's actual
   * implementation.
   */
  private Map<String, Object> settings = Collections.emptyMap();
}
