package io.github.lbarnkow.jarb;

import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.misc.Common;
import io.github.lbarnkow.jarb.misc.GuiceModule;

public class Main extends Common {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("# # # W E L C O M E # # #");

		Injector guice = Guice.createInjector(new GuiceModule());

		// TODO: Read configuration and enabled bots from config
		Runtime runtime = guice.getInstance(Runtime.class);

		BotManager botManager = guice.getInstance(BotManager.class);
		BotManagerConfiguration config = new BotManagerConfiguration();

		runtime.addShutdownHook(new Thread("ShutdownHook") {
			@Override
			public void run() {
				logger.info("JVM is shutting down, stopping all thread/services...");
				botManager.stop();
				logger.info("All threads/service stopped.");
			}
		});

		Bot demoBot = new DummyBot("demobot", "demobot");
//		Bot jiraBot = new DummyBot("jirabot", "jirabot");
//		Bot wikiBot = new DummyBot("wikibot", "wikibot");

		botManager.start(config, demoBot);
//		botManager.start(config, demoBot, jiraBot, wikiBot);
	}

	public static class DummyBot extends Bot {
		private final Credentials credentials;

		public DummyBot(String username, String password) {
			String passwordHash = DigestUtils.sha256Hex(password);
			this.credentials = new Credentials(username, passwordHash);
		}

		@Override
		public void initialize() {
		}

		@Override
		public String getName() {
			return credentials.getUsername();
		}

		@Override
		public Credentials getCredentials() {
			return credentials;
		}

		@Override
		public Optional<Message> offerMessage(Message message) {
			logger.error(message.toString());
			return Optional.empty();
		}

		@Override
		public boolean offerRoom(Room room) {
			return true;
		}
	}
}
