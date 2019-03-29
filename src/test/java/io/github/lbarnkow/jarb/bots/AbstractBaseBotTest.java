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

import static com.google.common.truth.Truth.assertThat;

import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.bots.help.HelpBot;
import io.github.lbarnkow.jarb.bots.nohelp.NoHelpBot;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class AbstractBaseBotTest {

  private static final String TEST_BOTNAME = "test bot";
  private static final String TEST_USERNAME = "username";
  private static final String TEST_PASSWORD = "password";

  private static final String TEST_HELP_TEXT = "help\nhelp\nhelp\n";

  private static final Credentials CREDENTIALS = new Credentials(TEST_USERNAME, TEST_PASSWORD);

  @Test
  void testHelpTextNonExistant() {
    // given
    NoHelpBot bot = new NoHelpBot();

    // when
    bot.initialize(TEST_BOTNAME, CREDENTIALS);

    // then
    assertThat(bot.getName()).isEqualTo(TEST_BOTNAME);
    assertThat(bot.getCredentials()).isSameAs(CREDENTIALS);
    assertThat(bot.getHelpText()).isNull();
  }

  @Test
  void testHelpText() {
    // given
    HelpBot bot = new HelpBot();

    // when
    bot.initialize(TEST_BOTNAME, CREDENTIALS);

    // then
    assertThat(bot.getName()).isEqualTo(TEST_BOTNAME);
    assertThat(bot.getCredentials()).isSameAs(CREDENTIALS);
    assertThat(bot.getHelpText()).hasSize(1);
    assertThat(bot.getHelpText().get(0).getText()).isEqualTo(TEST_HELP_TEXT);
  }

  @Test
  void testHelpTextIoException() {
    // given
    TestBot bot = new TestBot();

    // when
    bot.initialize(TEST_BOTNAME, CREDENTIALS);

    // then
    assertThat(bot.getName()).isEqualTo(TEST_BOTNAME);
    assertThat(bot.getCredentials()).isSameAs(CREDENTIALS);
    assertThat(bot.getHelpText()).isNull();
  }

  public static class TestBot extends NoHelpBot {
    @Override
    void loadHelpText() throws IOException {
      throw new IOException();
    }
  }
}
