package io.github.lbarnkow.rocketbot.taskmanager;

import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

public class MockHelper {
	public static List<Task> generateTaskMocks(int unused, int activating, int active, int deactivating, int dead) {
		List<Task> result = new ArrayList<>();

		for (int i = 0; i < unused; i++) {
			Task task = mock(Task.class);
			when(task.getState()).thenReturn(UNUSED);
			result.add(task);
		}
		for (int i = 0; i < activating; i++) {
			Task task = mock(Task.class);
			when(task.getState()).thenReturn(ACTIVATING);
			result.add(task);
		}
		for (int i = 0; i < active; i++) {
			Task task = mock(Task.class);
			when(task.getState()).thenReturn(ACTIVE);
			result.add(task);
		}
		for (int i = 0; i < deactivating; i++) {
			Task task = mock(Task.class);
			when(task.getState()).thenReturn(DEACTIVATING);
			result.add(task);
		}
		for (int i = 0; i < dead; i++) {
			Task task = mock(Task.class);
			when(task.getState()).thenReturn(DEAD);
			result.add(task);
		}

		return result;
	}

}
