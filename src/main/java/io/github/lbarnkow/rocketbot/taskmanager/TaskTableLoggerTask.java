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

	public TaskTableLoggerTask(TaskManager manager) {
		this.manager = manager;
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		while (true) {
			int unused = 0;
			int activating = 0;
			int active = 0;
			int deactivating = 0;
			int dead = 0;
			int total = 0;

			logger.info("Logging running background tasks");

			for (Task task : manager.getTasks()) {
				logger.debug("{} : {}", task.getName(), task.getState());

				if (task.getState() == UNUSED) {
					unused++;
				} else if (task.getState() == ACTIVATING) {
					activating++;
				} else if (task.getState() == ACTIVE) {
					active++;
				} else if (task.getState() == DEACTIVATING) {
					deactivating++;
				} else if (task.getState() == DEAD) {
					dead++;
				}
				total++;
			}

			logger.info("UNUSED......: {}", unused);
			logger.info("ACTIVATING..: {}", activating);
			logger.info("ACTIVE......: {}", active);
			logger.info("DEACTIVATING: {}", deactivating);
			logger.info("DEAD........: {}", dead);
			logger.info("TOTAL.......: {}", total);

			try {
				Thread.sleep(TASK_INTERVAL_MSEC);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
