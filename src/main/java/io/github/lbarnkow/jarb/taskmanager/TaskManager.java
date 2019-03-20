package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.misc.Common;

public class TaskManager extends Common {

	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

	private final Map<Task, TaskWrapper> tasks = new HashMap<>();

	private final Task[] managementTasks = new Task[] { new TaskTableLoggerTask(this), new DeadTaskPrunerTask(this) };

	public TaskManager() {
		start(managementTasks);
	}

	public synchronized void start(Task... tasks) {
		for (Task task : tasks) {
			TaskWrapper wrapper = new TaskWrapper(task);
			wrapper.startTask();
			this.tasks.put(task, wrapper);
		}
	}

	public synchronized void stopAll() {
		tasks.keySet().stream().forEach(task -> stop(task));
		waitForAllTasksToFinish();
	}

	public void stop(Task... tasks) {
		for (Task task : tasks) {
			this.tasks.get(task).stopTask();
		}
	}

	public void prune(Task... tasks) {
		for (Task task : tasks) {
			TaskWrapper wrapper = this.tasks.get(task);
			if (wrapper.getState() != DEAD) {
				throw new IllegalStateException("Only tasks in state " + DEAD + " can be pruned!");
			}
			this.tasks.remove(task);
		}
	}

	public int getTaskCount() {
		return tasks.size();
	}

	public int getNumberOfManagementTasks() {
		return managementTasks.length;
	}

	public Set<Task> getTasks() {
		return new HashSet<>(tasks.keySet());
	}

	public TaskState getTaskState(Task task) {
		return tasks.get(task).getState();
	}

	private void waitForAllTasksToFinish() {
		try {
			boolean done = false;
			while (!done) {
				done = true;
				for (TaskWrapper wrapper : tasks.values()) {
					if (wrapper.getState() != UNUSED && wrapper.getState() != DEAD) {
						done = false;
					}
				}
				Thread.sleep(50L);
			}

		} catch (InterruptedException e) {
			logger.error("Caught InterruptedException while waiting for all tasks to stop!");
		}
	}
}
