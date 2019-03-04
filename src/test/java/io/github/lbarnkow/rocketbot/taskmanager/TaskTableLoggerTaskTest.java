package io.github.lbarnkow.rocketbot.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.rocketbot.taskmanager.MockHelper.generateTaskMocks;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.lbarnkow.rocketbot.taskmanager.TaskTableLoggerTask.TaskStates;

class TaskTableLoggerTaskTest {

	@Test
	void testCountTasks() {
		// given
		List<Task> liveTasks = generateTaskMocks(5, 8, 3, 9, 0);
		List<Task> deadTasks = generateTaskMocks(0, 0, 0, 0, 10);

		List<Task> tasks = new ArrayList<>(liveTasks);
		tasks.addAll(deadTasks);
		assertThat(tasks).hasSize(35);

		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(tasks);

		TaskTableLoggerTask logger = new TaskTableLoggerTask(manager);

		// when
		TaskStates states = logger.countTasks();

		// then
		assertThat(states.unused).isEqualTo(5);
		assertThat(states.activating).isEqualTo(8);
		assertThat(states.active).isEqualTo(3);
		assertThat(states.deactivating).isEqualTo(9);
		assertThat(states.dead).isEqualTo(10);
	}

	@Test
	void testRun() throws InterruptedException {
		// given
		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(Collections.emptyList());

		TaskTableLoggerTask logger = new TaskTableLoggerTask(manager, 1L);

		// when
		logger.startTask();
		Thread.sleep(10L);
		logger.stopTask();

		// then
		verify(manager, atLeastOnce()).getTasks();
	}
}
