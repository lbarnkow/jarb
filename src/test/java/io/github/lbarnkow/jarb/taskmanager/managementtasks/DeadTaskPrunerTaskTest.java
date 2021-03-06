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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
