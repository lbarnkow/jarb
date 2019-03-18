package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.misc.Common;

public class TaskWrapper extends Common {

	static Logger logger = LoggerFactory.getLogger(TaskWrapper.class);

	private Task task = null;
	private TaskState state = UNUSED;
	private Thread thread = null;
	private Throwable lastError;

	public TaskWrapper(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("Paramter task cannot be null!");
		}

		this.task = task;
	}

	public void startTask() {
		if (state != UNUSED) {
			throw new IllegalStateException("Tasks can only be started once!");
		}

		state = ACTIVATING;
		thread = new Thread(() -> executeTask(), task.getName());
		thread.start();
	}

	private void executeTask() {
		try {
			task.initializeTask();
		} catch (Throwable t) {
			lastError = t;
			state = DEAD;
			logger.error("Initialization of Task '{}' raised an unexpected exception!", task.getName(), t);
			// TODO: notify listener about error!
			return;
		}

		state = ACTIVE;
		try {
			task.runTask();
		} catch (Throwable t) {
			lastError = t;
			logger.error("Task '{}' raised an unexpected exception!", task.getName(), t);
			// TODO: notify listener about error!
		}

		state = DEAD;
		logger.info("Task '{}' finished.", task.getName());
	}

	public void stopTask() {
		if (state == TaskState.ACTIVATING || state == TaskState.ACTIVE) {
			state = DEACTIVATING;
			thread.interrupt();
		}
	}

	public TaskState getState() {
		return state;
	}

	public final Throwable getLastError() {
		return lastError;
	}
}
