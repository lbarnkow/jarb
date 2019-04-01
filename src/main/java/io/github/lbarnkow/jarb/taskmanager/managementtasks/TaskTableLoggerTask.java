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

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import io.github.lbarnkow.jarb.taskmanager.TaskState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A management task that periodically prints a report of all tasks handled by a
 * given <code>TaskManager</code>. Depending on the active log level it prints
 * every tasks name and life-cycle state (level debug) and a summary showing the
 * number of tasks per life-cycle state (level info).
 *
 * @author lbarnkow
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class TaskTableLoggerTask extends AbstractBaseTask {
  private static final Logger logger = LoggerFactory.getLogger(TaskTableLoggerTask.class);

  private static final long TASK_INTERVAL_MSEC = 1000L * 60L * 5L; // repeat TASK every 5 minutes

  @ToString.Exclude
  private final TaskManager manager;
  private final long taskInterval;

  public TaskTableLoggerTask(TaskManager manager) {
    this(manager, TASK_INTERVAL_MSEC);
  }

  @Override
  public void runTask() throws Throwable {
    while (true) {
      logger.info("Logging running background tasks");

      TaskStates tasks = countTasks();

      logger.info("UNUSED......: {}", tasks.unused);
      logger.info("ACTIVATING..: {}", tasks.activating);
      logger.info("ACTIVE......: {}", tasks.active);
      logger.info("DEACTIVATING: {}", tasks.deactivating);
      logger.info("DEAD........: {}", tasks.dead);
      logger.info("TOTAL.......: {}", tasks.getTotal());

      try {
        Thread.sleep(taskInterval);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  TaskStates countTasks() {
    TaskStates result = new TaskStates();

    for (Task task : manager.getTasks()) {
      TaskState state = manager.getTaskState(task);
      logger.debug("{} : {}", task.getName(), state);

      if (state == UNUSED) {
        result.unused++;
      } else if (state == ACTIVATING) {
        result.activating++;
      } else if (state == ACTIVE) {
        result.active++;
      } else if (state == DEACTIVATING) {
        result.deactivating++;
      } else if (state == DEAD) {
        result.dead++;
      }
    }

    return result;
  }

  static class TaskStates {
    int unused;
    int activating;
    int active;
    int deactivating;
    int dead;

    int getTotal() {
      return unused + activating + active + deactivating + dead;
    }
  }
}
