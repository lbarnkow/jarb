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

package io.github.lbarnkow.jarb.bots;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.github.lbarnkow.jarb.api.Attachment;
import io.github.lbarnkow.jarb.api.Bot;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@ToString
public abstract class AbstractBaseBot implements Bot {
  @Getter
  private String name;
  @Getter
  private String username;
  @Getter
  private List<Attachment> helpText = null;

  @Override
  public Bot initialize(String name, String username) {
    try {
      loadHelpText();
    } catch (IOException e) {
      log.error("Failed to read 'HELP.md' for bot '{}'!", name, e);
    }

    this.name = name;
    this.username = username;

    return this;
  }

  void loadHelpText() throws IOException {
    val stream = getClass().getResourceAsStream("HELP.md");

    if (stream != null) {
      val reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
      String line = null;
      val sb = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }

      helpText = Lists.asList(Attachment.builder().text(sb.toString()).build(), new Attachment[0]);
    }
  }
}
