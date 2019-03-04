package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.misc.Common;

public class TaskManager extends Common {

	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

	private final List<Task> tasks = new ArrayList<>();

	private final Task[] managementTasks = new Task[] { new TaskTableLoggerTask(this), new DeadTaskPrunerTask(this) };

	public TaskManager() {
		start(managementTasks);
	}

	public synchronized void start(Task... tasks) {
		for (Task task : tasks) {
			task.startTask();
			this.tasks.add(task);
		}
	}

	public synchronized void stopAll() {
		tasks.stream().forEach(task -> stop(task));
		waitForAllTasksToFinish();
	}

	public void stop(Task... tasks) {
		for (Task task : tasks) {
			task.stopTask();
		}
	}

	public void prune(Task... tasks) {
		for (Task task : tasks) {
			if (task.getState() != DEAD) {
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

	public List<Task> getTasks() {
		return new ArrayList<>(tasks);
	}

	private void waitForAllTasksToFinish() {
		try {
			boolean finished = false;
			while (!finished) {
				finished = true;
				for (Task task : getTasks()) {
					if (task.getState() != UNUSED && task.getState() != DEAD) {
						finished = false;
					}
				}
				Thread.sleep(50L);
			}

		} catch (InterruptedException e) {
			logger.error("Caught InterruptedException while waiting for all tasks to stop!");
		}
	}
}
