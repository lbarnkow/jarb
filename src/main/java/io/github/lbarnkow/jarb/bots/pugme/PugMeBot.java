package io.github.lbarnkow.jarb.bots.pugme;

import static io.github.lbarnkow.jarb.api.MessageType.REGULAR_CHAT_MESSAGE;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import io.github.lbarnkow.jarb.api.Attachment;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.bots.AbstractBaseBot;
import lombok.Synchronized;
import lombok.ToString;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class PugMeBot extends AbstractBaseBot implements Bot {

	private Pattern REGEX = Pattern.compile("^\\@pugme(\\s\\w+)?\\s*$");

	private Instant lastUpdate;
	private List<String> pugsCache = new ArrayList<>(100);
	private Random random = new Random();

	@Inject
	private Client httpClient;

	@Override
	public boolean offerRoom(Room room) {
		// Don't auto-join anywhere, let others invite me
		return false;
	}

	@Override
	public Optional<Message> offerMessage(Message message) {
		Message reply = null;

		try {
			if (message.getType() == REGULAR_CHAT_MESSAGE) {
				val matcher = REGEX.matcher(message.getMessage());
				if (matcher.matches()) {
					val room = message.getRoom();
					val param = matcher.group(1);

					if (param == null) {
						reply = createPugReply(room, 1);
					} else if (param.trim().equals("bomb")) {
						reply = createPugReply(room, 5);
					}
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

		for (int i = 0; i < number; i++) {
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
		val response = httpClient.target("https://www.reddit.com/r") //
				.queryParam("sort", "top") //
				.queryParam("t", "week") //
				.queryParam("limit", "100") //
				.path("pugs.json") //
				.request(MediaType.APPLICATION_JSON) //
				.get();

		if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
			throw new RuntimeException(response.readEntity(String.class));
		}

		val posts = response.readEntity(RedditResponse.class);

		posts.getData().getChildren().stream() //
				.filter(child -> child.getData().is_video() == false) //
				.filter(child -> child.getData().getUrl().endsWith(".jpg"))
				.forEach(child -> pugsCache.add(child.getData().getUrl()));
	}
}