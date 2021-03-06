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
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class TaskManagerTest {

  private TaskManager manager = new TaskManager();
  private DummyTaskForUnitTesting task1 = new DummyTaskForUnitTesting(50L);
  private DummyTaskForUnitTesting task2 = new DummyTaskForUnitTesting(50L);
  private DummyTaskForUnitTesting task3 = new DummyTaskForUnitTesting(50L);

  @Test
  void testStart() throws InterruptedException {
    // given

    // when
    manager.start(Optional.empty(), true, task1, task2, task3);
    Thread.sleep(25L);
    final TaskState state1 = manager.getTaskState(task1);
    final TaskState state2 = manager.getTaskState(task2);
    final TaskState state3 = manager.getTaskState(task3);
    Thread.sleep(100L);

    // then
    assertThat(manager.getTaskCount()).isEqualTo(3);
    assertThat(manager.getTasks()).containsAllOf(task3, task2, task1);

    assertThat(task1.succeeded).isTrue();
    assertThat(task2.succeeded).isTrue();
    assertThat(task3.succeeded).isTrue();

    assertThat(state1).isEqualTo(ACTIVE);
    assertThat(state2).isEqualTo(ACTIVE);
    assertThat(state3).isEqualTo(ACTIVE);

    assertThat(manager.getTaskState(task1)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task2)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task3)).isEqualTo(DEAD);
  }

  @Test
  void testStopAll() throws InterruptedException {
    // given

    // when
    manager.start(Optional.empty(), true, task1, task2, task3);
    Thread.sleep(25L);
    manager.stopAll();

    // then
    assertThat(manager.getTaskState(task1)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task2)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task3)).isEqualTo(DEAD);

    assertThat(task1.succeeded).isFalse();
    assertThat(task2.succeeded).isFalse();
    assertThat(task3.succeeded).isFalse();
  }

  @Test
  void testStop() throws InterruptedException {
    // given

    // when
    manager.start(Optional.empty(), true, task1, task2, task3);
    Thread.sleep(25L);
    manager.stop(task1, task3);
    Thread.sleep(60L);

    // then
    assertThat(manager.getTaskState(task1)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task2)).isEqualTo(DEAD);
    assertThat(manager.getTaskState(task3)).isEqualTo(DEAD);

    assertThat(task1.succeeded).isFalse();
    assertThat(task2.succeeded).isTrue();
    assertThat(task3.succeeded).isFalse();
  }

  @Test
  void testPrune() {
    // given
    manager.start(Optional.empty(), true, task1);
    assertThat(manager.getTasks()).hasSize(1);
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
    manager.start(Optional.empty(), true, task1);
    Task[] tasksArray = manager.getTasks().toArray(new Task[] {});

    // when
    assertThrows(IllegalStateException.class, () -> manager.prune(tasksArray));

    // then
  }
}
