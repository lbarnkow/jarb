package io.github.lbarnkow.rocketbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.misc.Common;
import io.github.lbarnkow.rocketbot.misc.GuiceModule;

public class Main extends Common {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.info("# # # W E L C O M E # # #");

		Injector guice = Guice.createInjector(new GuiceModule());

		// TODO: Read configuration and enabled bots from config
		// public static final String SYNC_FILE_NAME = "/tmp/" +
		// ElectionConfig.class.getName() + ".json";
		// public static final File SYNC_FILE = new File(SYNC_FILE_NAME);

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

		// Bot jiraBot = null;
		// Bot wikiBot = null;
		// Bot jokeBot = null;
		Bot puppyBot = null;

		botManager.start(config, puppyBot);
	}
}
