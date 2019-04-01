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
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.bots.AbstractBaseBot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * <code>PugMeBot</code> replies with images of pugs fetched from reddit. It's a
 * fun proof-of-concept kind-of thing.
 *
 * @author lbarnkow
 */
@Slf4j
@ToString
public class PugMeBot extends AbstractBaseBot implements Bot {

  public static final int MAX_PUGS_PER_POST = 25;
  private static final String REGEX_BASE = "^\\s*@%BOTNAME%(\\s+(?:help|bomb))?(\\s+\\d+)?\\s*$";
  private Pattern regex;

  private Instant lastUpdate;
  private List<String> pugsCache = new ArrayList<>(100);
  private Random random = new Random();

  private Client jersey;

  /**
   * PugMeBot constructor.
   *
   * @param jerseyClient a jerseyClient instance to do the REST calls
   */
  @Inject
  public PugMeBot(Client jerseyClient) {
    super();

    this.jersey = jerseyClient;
  }

  @Override
  public Bot initialize(String name, String username) {
    val expression = REGEX_BASE.replace("%BOTNAME%", username);
    regex = Pattern.compile(expression, DOTALL);
    return super.initialize(name, username);
  }

  @Override
  public boolean offerRoom(Room room) {
    // Don't auto-join anywhere, let others invite me
    return false;
  }

  @Override
  public Optional<Message> offerMessage(Message message) {
    Message reply = null;

    if (message.getType() != REGULAR_CHAT_MESSAGE) {
      return Optional.empty(); // Only care about regular chat posts
    }
    if (message.getUser().getName().equals(getUsername())) {
      return Optional.empty(); // Don't process our own posts
    }

    try {
      Room room = message.getRoom();
      val matcher = regex.matcher(message.getMessage());
      if (matcher.matches()) {
        val action = parseAction(matcher.group(1));

        if (action.isEmpty()) {
          reply = createPugReply(room, 1);
        } else if ("bomb".equals(action)) {
          val count = parseInt(matcher.group(2), 5);
          reply = createPugReply(room, count);
        } else {
          reply = Message.builder().room(room).attachments(getHelpText()).build();
        }
      }
    } catch (Exception e) {
      log.error("Failed to handle message '{}'!", message, e);
      reply = Message.builder().room(message.getRoom())
          .message("*PugMeBot made a doody!* :poop: _(i.e. an internal error occured.)_").build();
    }

    return Optional.ofNullable(reply);
  }

  private Message createPugReply(Room room, int number) {
    List<Attachment> attachments = new ArrayList<>();

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
    val twentyFourHoursAgo = Instant.now().minus(24, HOURS);

    if (lastUpdate == null || lastUpdate.isBefore(twentyFourHoursAgo)) {
      pugsCache.clear();
      loadPugs();
      lastUpdate = Instant.now();
    }

    val index = random.nextInt(pugsCache.size());
    return pugsCache.get(index);
  }

  private void loadPugs() {
    // https://www.reddit.com/r/pugs.json?sort=top&t=week&limit=100
    val response = jersey.target("https://www.reddit.com/r") //
        .queryParam("sort", "top") //
        .queryParam("t", "week") //
        .queryParam("limit", "100") //
        .path("pugs.json") //
        .request(APPLICATION_JSON) //
        .get();

    if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
      throw new RuntimeException(response.readEntity(String.class));
    }

    val posts = response.readEntity(RedditResponse.class);

    posts.getData().getChildren().stream() //
        .filter(child -> child.getData().isVideo() == false) //
        .filter(child -> child.getData().getUrl().endsWith(".jpg"))
        .forEach(child -> pugsCache.add(child.getData().getUrl()));
  }

  private String parseAction(String s) {
    if (s != null) {
      return s.trim();
    }
    return "";
  }

  private int parseInt(String s, int or) {
    String data = (s != null) ? s.trim() : null;
    try {
      return Integer.parseInt(data);
    } catch (NumberFormatException e) {
      return or;
    }
  }
}
