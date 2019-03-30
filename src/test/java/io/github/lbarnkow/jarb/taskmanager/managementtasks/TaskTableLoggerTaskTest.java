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

package io.github.lbarnkow.jarb.taskmanager.managementtasks;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.taskmanager.MockHelper.generateTaskMocks;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskEndedCallback;
import io.github.lbarnkow.jarb.taskmanager.TaskEndedEvent;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import io.github.lbarnkow.jarb.taskmanager.managementtasks.TaskTableLoggerTask.TaskStates;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TaskTableLoggerTaskTest implements TaskEndedCallback {

  private Semaphore taskEndedSemaphore = new Semaphore(0);

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
    wrapper.startTask(Optional.of(this));
    Thread.sleep(30L);
    wrapper.stopTask();
    waitForTasksToEnd(1);

    // then
    assertThat(wrapper.getState()).isEqualTo(DEAD);
    verify(manager, atLeastOnce()).getTasks();
  }

  private void waitForTasksToEnd(int number) throws InterruptedException {
    boolean success = taskEndedSemaphore.tryAcquire(number, 5, TimeUnit.SECONDS);
    if (!success) {
      throw new RuntimeException(number + " tasks took too long to end!");
    }
  }

  @Override
  public void onTaskEnded(TaskEndedEvent event) {
    taskEndedSemaphore.release();
  }
}
