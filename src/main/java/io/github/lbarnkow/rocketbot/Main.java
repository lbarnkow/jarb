package io.github.lbarnkow.rocketbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.github.lbarnkow.rocketbot.guice.GuiceModule;
import io.github.lbarnkow.rocketbot.misc.Common;

public class Main extends Common {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		logger.trace("Hello, World!");

		Injector guice = Guice.createInjector(new GuiceModule());

		// TaskManager taskManager = guice.getInstance(TaskManager.class);
		// BotManager botManager = guice.getInstance(BotManager.class);
	}
}
