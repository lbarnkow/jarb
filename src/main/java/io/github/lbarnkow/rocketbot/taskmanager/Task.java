package io.github.lbarnkow.rocketbot.taskmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.misc.Common;

public abstract class Task extends Common {

	static Logger logger = LoggerFactory.getLogger(Task.class);

	private String name = getClass().getSimpleName() + "-thread";

	protected void initializeTask() throws Throwable {
	};

	protected abstract void runTask() throws Throwable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
