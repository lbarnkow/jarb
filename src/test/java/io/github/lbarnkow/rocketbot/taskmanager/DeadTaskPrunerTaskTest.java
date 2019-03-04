package io.github.lbarnkow.rocketbot.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.rocketbot.taskmanager.MockHelper.generateTaskMocks;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class DeadTaskPrunerTaskTest {

	@Test
	void testPruneTasks() {
		// given
		List<Task> liveTasks = generateTaskMocks(10, 10, 10, 10, 0);
		List<Task> deadTasks = generateTaskMocks(0, 0, 0, 0, 10);

		List<Task> tasks = new ArrayList<>(liveTasks);
		tasks.addAll(deadTasks);
		assertThat(tasks).hasSize(50);

		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(tasks);

		DeadTaskPrunerTask pruner = new DeadTaskPrunerTask(manager);

		// when
		int pruned = pruner.pruneTasks();

		// then
		assertThat(pruned).isEqualTo(10);
		for (Task task : deadTasks) {
			verify(manager).prune(task);
		}
		for (Task task : liveTasks) {
			verify(manager, times(0)).prune(task);
		}
	}

	@Test
	void testRun() throws InterruptedException {
		// given
		TaskManager manager = mock(TaskManager.class);
		when(manager.getTasks()).thenReturn(Collections.emptyList());

		DeadTaskPrunerTask pruner = new DeadTaskPrunerTask(manager, 1L);

		// when
		pruner.startTask();
		Thread.sleep(10L);
		pruner.stopTask();

		// then
		verify(manager, atLeastOnce()).getTasks();
	}
}
