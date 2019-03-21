package io.github.lbarnkow.jarb.taskmanager;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractBaseTask implements Task {
	@Getter
	@Setter
	private String name = getClass().getSimpleName() + "-thread";

	public void initializeTask() throws Throwable {
	};
}
