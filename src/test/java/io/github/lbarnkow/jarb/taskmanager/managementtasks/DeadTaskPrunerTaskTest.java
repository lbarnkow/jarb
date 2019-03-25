package io.github.lbarnkow.jarb.taskmanager.managementtasks;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.taskmanager.MockHelper.generateTaskMocks;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import io.github.lbarnkow.jarb.taskmanager.managementtasks.DeadTaskPrunerTask;

class DeadTaskPrunerTaskTest {

	@Test
	void testPruneTasks() {
		// given
		Map<Task, TaskWrapper> liveTasks = generateTaskMocks(10, 10, 10, 10, 0);
		Map<Task, TaskWrapper> deadTasks = generateTaskMocks(0, 0, 0, 0, 10);

		Map<Task, TaskWrapper> tasks = new HashMap<>();
		tasks.putAll(liveTasks);
		tasks.putAll(deadTasks);
		assertThat(tasks).hasSize(50);

		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(tasks.keySet());
		when(manager.getTaskState(Mockito.any(Task.class))).thenAnswer(invocation -> {
			Task task = (Task) invocation.getArgument(0);
			return tasks.get(task).getState();
		});

		DeadTaskPrunerTask pruner = new DeadTaskPrunerTask(manager);

		// when
		int pruned = pruner.pruneTasks();

		// then
		assertThat(pruned).isEqualTo(10);
		for (Task task : deadTasks.keySet()) {
			verify(manager).prune(task);
		}
		for (Task task : liveTasks.keySet()) {
			verify(manager, times(0)).prune(task);
		}
	}

	@Test
	void testRun() throws InterruptedException {
		// given
		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(Collections.emptySet());
		DeadTaskPrunerTask pruner = new DeadTaskPrunerTask(manager, 1L);
		TaskWrapper wrapper = new TaskWrapper(pruner);

		// when
		wrapper.startTask(null);
		Thread.sleep(10L);
		wrapper.stopTask();

		// then
		verify(manager, atLeastOnce()).getTasks();
	}
}
