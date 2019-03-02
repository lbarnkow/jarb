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
		logger.trace("Hello, World!");

		Injector guice = Guice.createInjector(new GuiceModule());

		// TaskManager taskManager = guice.getInstance(TaskManager.class);
		// BotManager botManager = guice.getInstance(BotManager.class);

		// TODO: Read configuration and enabled bots from config
		// public static final String SYNC_FILE_NAME = "/tmp/" +
		// ElectionConfig.class.getName() + ".json";
		// public static final File SYNC_FILE = new File(SYNC_FILE_NAME);

		Bot jiraBot = null;
		Bot wikiBot = null;
		Bot puppyBot = null;
		Bot jokeBot = null;

		BotManager botManager = null;

		botManager.initialize(jiraBot);
		botManager.initialize(wikiBot, puppyBot, jokeBot);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("JVM is shutting down, stopping all thread/services...");
				botManager.stop();
				logger.info("All threads/service stopped.");
			}
		});

		botManager.start();
	}
}
