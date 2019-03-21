package io.github.lbarnkow.jarb.taskmanager;

import lombok.Getter;
import lombok.Setter;

public abstract class Task {
	@Getter
	@Setter
	private String name = getClass().getSimpleName() + "-thread";

	protected void initializeTask() throws Throwable {
	};

	protected abstract void runTask() throws Throwable;
}
