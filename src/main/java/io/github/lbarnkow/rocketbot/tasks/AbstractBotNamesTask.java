package io.github.lbarnkow.rocketbot.tasks;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.taskmanager.Task;

public abstract class AbstractBotNamesTask extends Task {

	private final Bot bot;

	public AbstractBotNamesTask(Bot bot) {
		this.bot = bot;

		this.setName(getClass().getSimpleName() + "-" + bot.getName() + "-thread");
	}

	public Bot getBot() {
		return bot;
	}

}
