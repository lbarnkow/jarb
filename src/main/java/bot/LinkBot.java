package bot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import bot.rocketchat.Message;
import bot.rocketchat.rest.requests.MessageSendRequest;
import bot.rocketchat.rest.requests.MessageSendRequest.Attachment;

public class LinkBot extends Bot {

	@Override
	public Message onRocketChatClientMessage(Message message) {
		// TODO: inspect and respond
		Instant threshold = Instant.now().minus(5, ChronoUnit.MINUTES);
		if (message.getTimestamp().isBefore(threshold)) {
			System.out.println("Message too old, discarding...");
		} else {
			System.out.println(message);

			MessageSendRequest.Attachment a = new Attachment("JIRA-1234", "https://www.github.com/",
					"The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog.\n\nThe quick brown fox jumps over the lazy dog. The quick brown fox jumps over the lazy dog.",
					"http://jira.system.local/s/-oborit/74004/46a1907a29fbb094601285ce01831a25/_/favicon.ico");

			System.out.println(sendMessage(message.getRoomId(), null, a));
		}
		return null;
	}
}
