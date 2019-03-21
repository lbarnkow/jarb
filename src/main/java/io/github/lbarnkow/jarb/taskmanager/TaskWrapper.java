package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class TaskWrapper {
	static Logger logger = LoggerFactory.getLogger(TaskWrapper.class);

	private final Task task;
	private Thread thread = null;
	private TaskState state = UNUSED;
	private Throwable lastError;

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
}
