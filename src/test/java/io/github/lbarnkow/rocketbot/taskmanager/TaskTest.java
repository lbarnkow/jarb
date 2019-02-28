package io.github.lbarnkow.rocketbot.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.rocketbot.taskmanager.TaskState.UNUSED;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TaskTest {

	@Test
	void testSuccessfulTaskLifecycle() throws InterruptedException {
		// given
		DummyTask task = new DummyTask(10L, false, false);

		// when
		task.start();
		Thread.sleep(5L);
		task.stop();
		Thread.sleep(10L);

		// then
		assertThat(task.stateOnConstruction).isEqualTo(UNUSED);
		assertThat(task.stateOnInitialization).isEqualTo(ACTIVATING);
		assertThat(task.stateOnRun).isEqualTo(ACTIVE);
		assertThat(task.stateOnInterruption).isEqualTo(DEACTIVATING);
		assertThat(task.getState()).isEqualTo(DEAD);
		assertThat(task.getLastError()).isNull();
	}

	@Test
	void testTryTaskTwice() {
		// given
		DummyTask task = new DummyTask(0L, false, false);

		// when
		task.start();

		// then
		assertThrows(IllegalStateException.class, () -> task.start());
	}

	@Test
	void testFailedInitialization() throws InterruptedException {
		// given
		DummyTask task = new DummyTask(10L, true, false);

		// when
		task.start();
		Thread.sleep(5L);

		// then
		assertThat(task.getLastError()).isInstanceOf(DummyTask.InitializeException.class);
	}

	@Test
	void testFailedRun() throws InterruptedException {
		// given
		DummyTask task = new DummyTask(10L, false, true);

		// when
		task.start();
		Thread.sleep(5L);

		// then
		assertThat(task.getLastError()).isInstanceOf(DummyTask.RunException.class);
	}

}
