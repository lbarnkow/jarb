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

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;

import com.google.common.annotations.VisibleForTesting;
import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A management task that periodically removes all <code>DEAD</code> tasks from
 * a given <code>TaskManager</code>.
 *
 * @author lbarnkow
 */
@ToString
@AllArgsConstructor
@Slf4j
public class DeadTaskPrunerTask extends AbstractBaseTask {
  /**
   * The default interval in which dead tasks should be pruned.
   */
  private static final long TASK_INTERVAL_MS = 1000L * 60L * 5L; // repeat TASK every 5 minutes

  /**
   * Externally supplied <code>TaskManager</code> to find and prune dead tasks
   * for.
   */
  @ToString.Exclude
  private final TaskManager manager;

  /**
   * The interval in which dead tasks should be pruned.
   */
  private final long taskInterval;

  /**
   * Constructs a new instance for a given <code>TaskManager</code>.
   *
   * @param manager the <code>TaskManager</code>
   */
  public DeadTaskPrunerTask(final TaskManager manager) {
    this(manager, TASK_INTERVAL_MS);
  }

  @Override
  public void runTask() throws Exception {
    while (true) {
      pruneTasks();

      try {
        Thread.sleep(taskInterval);
      } catch (final InterruptedException e) {
        break;
      }
    }
  }

  @VisibleForTesting
  int pruneTasks() {
    int pruned = 0;

    for (final Task task : manager.getTasks()) {
      if (manager.getTaskState(task) == DEAD) {
        log.debug("Pruning task '{}' in state '{}'.", task.getName(), DEAD);
        pruned++;
        manager.prune(task);
      }
    }

    if (pruned > 0) {
      log.info("Pruned {} background tasks in state '{}'.", pruned, DEAD);
    }

    return pruned;
  }
}
