package io.github.lbarnkow.jarb.tasks;

import io.github.lbarnkow.jarb.taskmanager.Task;

public abstract class AbstractBaseTask extends Task {

	public AbstractBaseTask(String nameAddition) {
		this.setName(getClass().getSimpleName() + "-" + nameAddition + "-thread");
	}

}
