package io.github.lbarnkow.jarb.taskmanager;

public interface Task {
	String getName();

	void initializeTask() throws Throwable;

	void runTask() throws Throwable;
}
