package io.github.lbarnkow.rocketbot.taskmanager;

import java.util.ArrayList;
import java.util.List;

import io.github.lbarnkow.rocketbot.misc.Common;

public class TaskManager extends Common {
	private final List<Task> tasks = new ArrayList<>();

	public synchronized void start(Task... tasks) {
		for (Task task : tasks) {
			task.startTask();
			this.tasks.add(task);
		}
	}

	public synchronized void stopAll() {
		for (Task task : tasks) {
			stop(task);
		}
	}

	public void stop(Task... tasks) {
		for (Task task : tasks) {
			task.stopTask();
		}
	}

	public int getTaskCount() {
		return tasks.size();
	}

	public List<Task> getTasks() {
		return new ArrayList<>(tasks);
	}
}
