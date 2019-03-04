package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.misc.Common;

public abstract class Task extends Common {

	static Logger logger = LoggerFactory.getLogger(Task.class);

	private TaskState state = UNUSED;
	private Thread thread = null;
	private String name = getClass().getSimpleName() + "-thread";

	private Throwable lastError;

	// TODO: remove?!
	protected abstract void initializeTask() throws Throwable;

	protected abstract void runTask() throws Throwable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	final void startTask() {
		if (state != UNUSED) {
			throw new IllegalStateException("Tasks can only be started once!");
		}

		state = ACTIVATING;
		thread = new Thread(() -> executeTask(), getName());
		thread.start();
	}

	private void executeTask() {
		try {
			initializeTask();
		} catch (Throwable t) {
			lastError = t;
			state = DEAD;
			logger.error("Initialization of Task '{}' raised an unexpected exception!", getClass().getSimpleName(), t);
			return;
		}

		state = ACTIVE;
		try {
			runTask();
		} catch (Throwable t) {
			lastError = t;
			logger.error("Task '{}' raised an unexpected exception!", getClass().getSimpleName(), t);
		}

		state = DEAD;
		logger.info("Task '{}' finished.", getName());
	}

	final void stopTask() {
		if (state == TaskState.ACTIVATING || state == TaskState.ACTIVE) {
			state = DEACTIVATING;
			thread.interrupt();
		}
	}

	public final TaskState getState() {
		return state;
	}

	final Throwable getLastError() {
		return lastError;
	}
}
