package io.github.lbarnkow.rocketbot.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaskManagerTest {

	private TaskManager manager = new TaskManager();
	private DummyTask task1 = new DummyTask(50L);
	private DummyTask task2 = new DummyTask(50L);
	private DummyTask task3 = new DummyTask(50L);

	@Test
	void testStart() throws InterruptedException {
		// given

		// when
		manager.start(task1, task2, task3);
		Thread.sleep(20L);
		TaskState state1 = task1.getState();
		TaskState state2 = task1.getState();
		TaskState state3 = task1.getState();
		Thread.sleep(100L);

		// then
		assertThat(manager.getTaskCount() - manager.getNumberOfManagementTasks()).isEqualTo(3);
		assertThat(manager.getTasks()).containsAllOf(task3, task2, task1);

		assertThat(task1.succeeded).isTrue();
		assertThat(task2.succeeded).isTrue();
		assertThat(task3.succeeded).isTrue();

		assertThat(state1).isEqualTo(ACTIVE);
		assertThat(state2).isEqualTo(ACTIVE);
		assertThat(state3).isEqualTo(ACTIVE);

		assertThat(task1.getState()).isEqualTo(DEAD);
		assertThat(task2.getState()).isEqualTo(DEAD);
		assertThat(task3.getState()).isEqualTo(DEAD);
	}

	@Test
	void testStopAll() throws InterruptedException {
		// given

		// when
		manager.start(task1, task2, task3);
		manager.stopAll();
		Thread.sleep(25L);

		// then
		assertThat(task1.getState()).isEqualTo(DEAD);
		assertThat(task2.getState()).isEqualTo(DEAD);
		assertThat(task3.getState()).isEqualTo(DEAD);

		assertThat(task1.succeeded).isFalse();
		assertThat(task2.succeeded).isFalse();
		assertThat(task3.succeeded).isFalse();
	}

	@Test
	void testStop() throws InterruptedException {
		// given

		// when
		manager.start(task1, task2, task3);
		manager.stop(task1, task3);
		Thread.sleep(60L);

		// then
		assertThat(task1.getState()).isEqualTo(DEAD);
		assertThat(task2.getState()).isEqualTo(DEAD);
		assertThat(task3.getState()).isEqualTo(DEAD);

		assertThat(task1.succeeded).isFalse();
		assertThat(task2.succeeded).isTrue();
		assertThat(task3.succeeded).isFalse();
	}

	@Test
	void testPrune() {
		// given
		assertThat(manager.getTasks()).hasSize(manager.getNumberOfManagementTasks());
		Task[] tasksArray = manager.getTasks().toArray(new Task[] {});

		// when
		manager.stopAll();
		manager.prune(tasksArray);

		// then
		assertThat(manager.getTasks()).isEmpty();
		assertThat(manager.getTaskCount());
	}

	@Test
	void testPruneLiveTask() {
		// given
		Task[] tasksArray = manager.getTasks().toArray(new Task[] {});

		// when
		assertThrows(IllegalStateException.class, () -> manager.prune(tasksArray));

		// then
	}
}
