package io.github.lbarnkow.jarb.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class TaskWrapperTest implements TaskEndedCallback {

  private List<TaskEndedEvent> eventLog = new ArrayList<>();

  @Test
  void testSuccessfulTaskWrapperLifecycle() throws InterruptedException {
    // given
    DummyTaskForUnitTesting task = new DummyTaskForUnitTesting(20L, false, false);
    TaskWrapper wrapper = new TaskWrapper(task);
    task.captureStateBeforeInitialization(wrapper);

    // when
    wrapper.startTask(Optional.of(this));
    Thread.sleep(5L);
    wrapper.stopTask();
    Thread.sleep(40L);

    // then
    assertThat(task.stateBeforeInitialization).isEqualTo(UNUSED);
    assertThat(task.stateOnInitialization).isEqualTo(ACTIVATING);
    assertThat(task.stateOnRun).isEqualTo(ACTIVE);
    assertThat(task.stateOnInterruption).isEqualTo(DEACTIVATING);
    assertThat(wrapper.getState()).isEqualTo(DEAD);
    assertThat(wrapper.getLastError()).isNull();
  }

  @Test
  void testTryStartingTaskWrapperTwice() {
    // given
    DummyTaskForUnitTesting task = new DummyTaskForUnitTesting(0L, false, false);
    TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.of(this));

    // then
    assertThrows(IllegalStateException.class, () -> wrapper.startTask(Optional.of(this)));
  }

  @Test
  void testTaskThrowingExceptionDuringInitialization() throws InterruptedException {
    // given
    DummyTaskForUnitTesting task = new DummyTaskForUnitTesting(10L, true, false);
    TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.of(this));
    Thread.sleep(15L);

    // then
    assertThat(wrapper.getLastError())
        .isInstanceOf(DummyTaskForUnitTesting.InitializeException.class);
  }

  @Test
  void testTaskThrowingExceptionDuringRun() throws InterruptedException {
    // given
    DummyTaskForUnitTesting task = new DummyTaskForUnitTesting(10L, false, true);
    TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.of(this));
    Thread.sleep(25L);

    // then
    assertThat(wrapper.getLastError()).isInstanceOf(DummyTaskForUnitTesting.RunException.class);
  }

  @Override
  public void onTaskEnded(TaskEndedEvent event) {
    eventLog.add(event);
  }
}
