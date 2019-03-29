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
