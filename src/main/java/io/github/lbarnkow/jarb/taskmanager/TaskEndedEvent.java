package io.github.lbarnkow.jarb.taskmanager;

import lombok.Value;

@Value
public class TaskEndedEvent {
	private Task task;
	private TaskState state;
	private Throwable lastError;
}
