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

	private Throwable lastError;

	protected abstract void initialize() throws Throwable;

	protected abstract void run() throws Throwable;

	final void start() {
		if (state != UNUSED) {
			throw new IllegalStateException("Tasks can only be started once!");
		}

		state = ACTIVATING;
		thread = new Thread(() -> execute());
		thread.start();
	}

	private void execute() {
		try {
			initialize();
		} catch (Throwable t) {
			lastError = t;
			state = DEAD;
			logger.error("Initialization of Task '{}' raised an unexpected exception!", getClass().getSimpleName(), t);
			return;
		}

		state = ACTIVE;
		try {
			run();
		} catch (Throwable t) {
			lastError = t;
			logger.error("Task '{}' raised an unexpected exception!", getClass().getSimpleName(), t);
		}
		state = DEAD;
	}

	final void stop() {
		state = DEACTIVATING;
		thread.interrupt();
	}

	public final TaskState getState() {
		return state;
	}

	final Throwable getLastError() {
		return lastError;
	}
}
