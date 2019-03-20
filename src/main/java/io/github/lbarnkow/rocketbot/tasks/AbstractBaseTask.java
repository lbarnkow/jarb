package io.github.lbarnkow.rocketbot.tasks;

import io.github.lbarnkow.rocketbot.taskmanager.Task;

public abstract class AbstractBaseTask extends Task {

	public AbstractBaseTask(String nameAddition) {
		this.setName(getClass().getSimpleName() + "-" + nameAddition + "-thread");
	}

}
