package io.github.lbarnkow.jarb.bots;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.github.lbarnkow.jarb.api.Attachment;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
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
  private Credentials credentials;
  @Getter
  private List<Attachment> helpText = null;

  @Override
  public Bot initialize(String name, Credentials credentials) {
    try {
      loadHelpText();
    } catch (IOException e) {
      log.error("Failed to read 'HELP.md' for bot '{}'!", name, e);
    }

    this.name = name;
    this.credentials = credentials;

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
