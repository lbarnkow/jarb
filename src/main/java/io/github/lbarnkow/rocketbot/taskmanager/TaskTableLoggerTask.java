package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskTableLoggerTask extends Task {
	private static final Logger logger = LoggerFactory.getLogger(TaskTableLoggerTask.class);

	private static final long TASK_INTERVAL_MSEC = 1000L * 60L * 5L; // repeat TASK every 5 minutes

	private final TaskManager manager;
	private final long taskInterval;

	public TaskTableLoggerTask(TaskManager manager) {
		this(manager, TASK_INTERVAL_MSEC);
	}

	TaskTableLoggerTask(TaskManager manager, long taskInterval) {
		this.manager = manager;
		this.taskInterval = taskInterval;
	}

	@Override
	protected void runTask() throws Throwable {
		while (true) {
			logger.info("Logging running background tasks");

			TaskStates tasks = countTasks();

			logger.info("UNUSED......: {}", tasks.unused);
			logger.info("ACTIVATING..: {}", tasks.activating);
			logger.info("ACTIVE......: {}", tasks.active);
			logger.info("DEACTIVATING: {}", tasks.deactivating);
			logger.info("DEAD........: {}", tasks.dead);
			logger.info("TOTAL.......: {}", tasks.getTotal());

			try {
				Thread.sleep(taskInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	TaskStates countTasks() {
		TaskStates result = new TaskStates();

		for (Task task : manager.getTasks()) {
			logger.debug("{} : {}", task.getName(), task.getState());

			if (task.getState() == UNUSED) {
				result.unused++;
			} else if (task.getState() == ACTIVATING) {
				result.activating++;
			} else if (task.getState() == ACTIVE) {
				result.active++;
			} else if (task.getState() == DEACTIVATING) {
				result.deactivating++;
			} else if (task.getState() == DEAD) {
				result.dead++;
			}
		}

		return result;
	}

	static class TaskStates {
		int unused = 0;
		int activating = 0;
		int active = 0;
		int deactivating = 0;
		int dead = 0;

		int getTotal() {
			return unused + activating + active + deactivating + dead;
		}
	}
}
