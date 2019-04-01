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
 * Central jarb configuration loaded from YAML.
 *
 * @author lbarnkow
 */
@JarbJsonSettings
@Data
public class BotConfiguration {
  private static final String DEFAULT_NAME = "demobot";
  private static final String DEFAULT_QUALIFIED_CLASS_NAME = DummyBot.class.getName();
  private static final Credentials DEFAULT_CREDENTIALS =
      new Credentials("demobot", DigestUtils.sha256Hex("demobot"));

  private String name = DEFAULT_NAME;
  private String qualifiedClassName = DEFAULT_QUALIFIED_CLASS_NAME;
  private Credentials credentials = DEFAULT_CREDENTIALS;
  private Map<String, Object> settings = Collections.emptyMap();
}
