package io.github.lbarnkow.jarb.taskmanager;

public interface TaskEndedCallback {
	void onTaskEnded(TaskEndedEvent event);
}