package io.github.lbarnkow.rocketbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.misc.Common;

public class BotManager extends Common {

	private static final Logger logger = LoggerFactory.getLogger(BotManager.class);

	public void initialize(Bot... bots) {
	}

	public void stop() {
	}

	public void start() {
		// start LeaseManager and wait for leader status

		// connect via ws

		// login all bots (login task per bot)
		// -- if a login fails, shutdown!

		// catch up on all channels for all bots

		// add real-time subscription to all joined rooms for all bots

		// start room-join-task (per bot?)

		// done starting :)
	}
}
