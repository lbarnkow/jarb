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
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Abstract base implementation of interface <code>Bot</code> to handle
 * properties like <code>name</code> and <code>username</code>. Additionally,
 * this base class also loads help texts from a resource named 'HELP.md' which
 * can then be accessed by the sub class.
 *
 * @author lbarnkow
 */
@Slf4j
@ToString
@NoArgsConstructor
public abstract class AbstractBaseBot implements Bot {
  /**
   * Stores the logical jarb run-time name.
   */
  @Getter
  private String name;

  /**
   * Stores the user name on the chat server.
   */
  @Getter
  private String username;

  /**
   * Stores the help text from 'HELP.md'.
   */
  @Getter
  private List<Attachment> helpText;

  @Override
  public Bot initialize(final String name, final String username) {
    try {
      loadHelpText();
    } catch (final IOException e) {
      log.error("Failed to read 'HELP.md' for bot '{}'!", name, e);
    }

    this.name = name;
    this.username = username;

    return this;
  }

  /**
   * Tries to load 'HELP.md' from the same package as the current bots class.
   *
   * @throws IOException on IO errors
   */
  protected void loadHelpText() throws IOException {
    final val stream = getClass().getResourceAsStream("HELP.md");

    if (stream != null) {
      final val reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
      final val builder = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        builder.append(line).append("\n");
        line = reader.readLine();
      }

      helpText =
          Lists.asList(Attachment.builder().text(builder.toString()).build(), new Attachment[0]);
    }
  }
}
