package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadTaskPrunerTask extends Task {
	private static final Logger logger = LoggerFactory.getLogger(DeadTaskPrunerTask.class);

	private static final long TASK_INTERVAL_MSEC = 1000L * 60L * 5L; // repeat TASK every 5 minutes

	private final TaskManager manager;

	public DeadTaskPrunerTask(TaskManager manager) {
		this.manager = manager;
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		while (true) {
			int pruned = 0;

			for (Task task : manager.getTasks()) {
				if (task.getState() == DEAD) {
					logger.debug("Pruning task '{}' in state '{}'.", task.getName(), task.getState());
					pruned++;
					manager.prune(task);
				}
			}

			if (pruned > 0) {
				logger.info("Pruned {} background tasks in state '{}'.", pruned, DEAD);
			}

			try {
				Thread.sleep(TASK_INTERVAL_MSEC);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
