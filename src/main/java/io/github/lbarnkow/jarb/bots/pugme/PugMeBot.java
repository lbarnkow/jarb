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

package io.github.lbarnkow.jarb.bots.pugme;

import static io.github.lbarnkow.jarb.api.MessageType.REGULAR_CHAT_MESSAGE;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.regex.Pattern.DOTALL;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import io.github.lbarnkow.jarb.api.Attachment;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.BotException;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.bots.AbstractBaseBot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * <code>PugMeBot</code> replies with images of pugs fetched from reddit. It's a
 * fun proof-of-concept kind-of thing.
 *
 * @author lbarnkow
 */
@Slf4j
@ToString
public class PugMeBot extends AbstractBaseBot implements Bot {

  /**
   * Limits the maximum number of pug images deliverd in a single "pug bomb"
   * reply.
   */
  public static final int MAX_PUGS_PER_POST = 25;

  /**
   * The base regular expression pattern to parse incoming messages. Contains a
   * place holder ("%BOTNAME%") that will be replaced a runtime.
   */
  private static final String REGEX_BASE = "^\\s*@%BOTNAME%(\\s+(?:help|bomb))?(\\s+\\d+)?\\s*$";

  /**
   * The regular expression pattern instance to use to parse incoming messages
   * (see methond <code>initialize</code>).
   */
  private transient Pattern regex;

  /**
   * The time stamp marking the last update of pug picture URLs from reddit.
   */
  private transient Instant lastUpdate;
  /**
   * A local cache containing pug picture URLs.
   */
  private final transient List<String> pugsCache = new ArrayList<>(100);

  /**
   * RNG to select pug picture URLs from the cache.
   */
  private final transient Random random;

  /**
   * The REST client instance to access reddit.
   */
  private final transient Client jersey;

  /**
   * PugMeBot constructor.
   *
   * @param jerseyClient a jerseyClient instance to do the REST calls
   */
  @Inject
  public PugMeBot(final Random random, final Client jerseyClient) {
    super();

    this.random = random;
    this.jersey = jerseyClient;
  }

  @Override
  public Bot initialize(final String name, final String username) {
    final String expression = REGEX_BASE.replace("%BOTNAME%", username);
    regex = Pattern.compile(expression, DOTALL);
    return super.initialize(name, username);
  }

  @Override
  public boolean offerRoom(final Room room) {
    // Don't auto-join anywhere, let others invite me
    return false;
  }

  @Override
  public Optional<Message> offerMessage(final Message message) {
    Message reply = null;

    // Only care about regular chat posts not originating from ourselves.
    if (message.getType() == REGULAR_CHAT_MESSAGE
        && !message.getUser().getName().equals(getUsername())) {
      try {
        final Room room = message.getRoom();
        final Matcher matcher = regex.matcher(message.getText());
        if (matcher.matches()) {
          final String action = trimAction(matcher.group(1));

          if (action.isEmpty()) {
            reply = createPugReply(room, 1);
          } else if ("bomb".equals(action)) {
            final int count = parseInt(matcher.group(2), 5);
            reply = createPugReply(room, count);
          } else {
            reply = Message.builder().room(room).attachments(getHelpText()).build();
          }
        }
      } catch (final Exception e) {
        log.error("Failed to handle message '{}'!", message, e);
        reply = Message.builder().room(message.getRoom())
            .text("*PugMeBot made a doody!* :poop: _(i.e. an internal error occured.)_").build();
      }
    }

    return Optional.ofNullable(reply);
  }

  private Message createPugReply(final Room room, final int number) {
    final List<Attachment> attachments = new ArrayList<>();

    int num = number;
    if (num == 0) {
      num = 1;
    } else if (num > MAX_PUGS_PER_POST) {
      num = MAX_PUGS_PER_POST;
    }

    for (int i = 0; i < num; i++) {
      attachments.add(Attachment.builder().imageUrl(selectImageUrl()).build());
    }

    return Message.builder().room(room).attachments(attachments).build();
  }

  @Synchronized
  private String selectImageUrl() {
    final Instant oneDayAgo = Instant.now().minus(24, HOURS);

    if (lastUpdate == null || lastUpdate.isBefore(oneDayAgo)) {
      pugsCache.clear();
      loadPugs();
      lastUpdate = Instant.now();
    }

    final int index = random.nextInt(pugsCache.size());
    return pugsCache.get(index);
  }

  private void loadPugs() {
    // https://www.reddit.com/r/pugs.json?sort=top&t=week&limit=100
    final Response response = jersey.target("https://www.reddit.com/r") //
        .queryParam("sort", "top") //
        .queryParam("t", "week") //
        .queryParam("limit", "100") //
        .path("pugs.json") //
        .request(APPLICATION_JSON) //
        .get();

    if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
      throw new BotException(response.readEntity(String.class));
    }

    final RedditResponse posts = response.readEntity(RedditResponse.class);

    posts.getData().getChildren().stream() //
        .filter(child -> !child.getData().isVideo()) //
        .filter(child -> child.getData().getUrl().endsWith(".jpg"))
        .forEach(child -> pugsCache.add(child.getData().getUrl()));
  }

  private String trimAction(final String string) {
    String result = "";

    if (string != null) {
      result = string.trim();
    }

    return result;
  }

  private int parseInt(final String string, final int defaultValue) {
    final String data = string == null ? null : string.trim();
    int result;

    try {
      result = Integer.parseInt(data);
    } catch (final NumberFormatException e) {
      result = defaultValue;
    }

    return result;
  }
}
