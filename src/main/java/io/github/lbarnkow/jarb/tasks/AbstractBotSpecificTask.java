package io.github.lbarnkow.jarb.tasks;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBotSpecificTask extends AbstractBaseTask {

	@Getter
	private final Bot bot;

	public AbstractBotSpecificTask(Bot bot) {
		this.bot = bot;
		this.setName(getClass().getSimpleName() + "-" + bot.getName() + "-thread");
	}
}
