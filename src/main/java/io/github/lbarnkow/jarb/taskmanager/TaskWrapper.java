package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.val;

@Data
public class TaskWrapper {
	static Logger logger = LoggerFactory.getLogger(TaskWrapper.class);

	private final Task task;
	private Thread thread = null;
	private TaskState state = UNUSED;
	private Throwable lastError;

	public void startTask(Optional<TaskEndedCallback> callback) {
		if (state != UNUSED) {
			throw new IllegalStateException("Tasks can only be started once!");
		}

		val cb = callback != null ? callback : Optional.<TaskEndedCallback>empty();

		state = ACTIVATING;
		thread = new Thread(() -> executeTask(cb), task.getName());
		thread.start();
	}

	private void executeTask(Optional<TaskEndedCallback> callback) {
		try {
			task.initializeTask();
		} catch (Throwable t) {
			handleError("Initialization", t, callback);
			return;
		}

		state = ACTIVE;
		try {
			task.runTask();
		} catch (Throwable t) {
			handleError("Execution", t, callback);
			return;
		}

		state = DEAD;
		logger.info("Task '{}' finished.", task.getName());
		if (callback.isPresent()) {
			TaskEndedEvent event = TaskEndedEvent.builder().task(task).state(state).lastError(Optional.empty()).build();
			callback.get().onTaskEnded(event);
		}
	}

	public void stopTask() {
		if (state == TaskState.ACTIVATING || state == TaskState.ACTIVE) {
			state = DEACTIVATING;
			thread.interrupt();
		}
	}

	private void handleError(String stage, Throwable t, Optional<TaskEndedCallback> callback) {
		lastError = t;
		logger.error("{} of task '{}' raised an unexpected exception!", stage, task.getName(), t);
		state = DEAD;
		if (callback.isPresent()) {
			TaskEndedEvent event = TaskEndedEvent.builder().task(task).state(state).lastError(Optional.of(t)).build();
			callback.get().onTaskEnded(event);
		}
	}
}
