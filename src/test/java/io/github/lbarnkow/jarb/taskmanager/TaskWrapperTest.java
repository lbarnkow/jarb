/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
