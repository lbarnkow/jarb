package io.github.lbarnkow.jarb.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.taskmanager.MockHelper.generateTaskMocks;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.lbarnkow.jarb.taskmanager.TaskTableLoggerTask.TaskStates;

class TaskTableLoggerTaskTest {

	@Test
	void testCountTasks() {
		// given
		Map<Task, TaskWrapper> liveTasks = generateTaskMocks(5, 8, 3, 9, 0);
		Map<Task, TaskWrapper> deadTasks = generateTaskMocks(0, 0, 0, 0, 10);

		Map<Task, TaskWrapper> tasks = new HashMap<>();
		tasks.putAll(liveTasks);
		tasks.putAll(deadTasks);
		assertThat(tasks).hasSize(35);

		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(tasks.keySet());
		when(manager.getTaskState(Mockito.any(Task.class))).thenAnswer(invocation -> {
			Task task = (Task) invocation.getArgument(0);
			return tasks.get(task).getState();
		});

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
		when(manager.getTasks()).thenReturn(Collections.emptySet());

		TaskTableLoggerTask task = new TaskTableLoggerTask(manager, 1L);
		TaskWrapper wrapper = new TaskWrapper(task);

		// when
		wrapper.startTask();
		Thread.sleep(10L);
		wrapper.stopTask();
		Thread.sleep(10L);

		// then
		assertThat(wrapper.getState()).isEqualTo(DEAD);
		verify(manager, atLeastOnce()).getTasks();
	}
}
