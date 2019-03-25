package io.github.lbarnkow.jarb.taskmanager.managementtasks;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.taskmanager.TaskState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class TaskTableLoggerTask extends AbstractBaseTask {
	private static final Logger logger = LoggerFactory.getLogger(TaskTableLoggerTask.class);

	private static final long TASK_INTERVAL_MSEC = 1000L * 60L * 5L; // repeat TASK every 5 minutes

	@ToString.Exclude
	private final TaskManager manager;
	private final long taskInterval;

	public TaskTableLoggerTask(TaskManager manager) {
		this(manager, TASK_INTERVAL_MSEC);
	}

	@Override
	public void runTask() throws Throwable {
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
			TaskState state = manager.getTaskState(task);
			logger.debug("{} : {}", task.getName(), state);

			if (state == UNUSED) {
				result.unused++;
			} else if (state == ACTIVATING) {
				result.activating++;
			} else if (state == ACTIVE) {
				result.active++;
			} else if (state == DEACTIVATING) {
				result.deactivating++;
			} else if (state == DEAD) {
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
