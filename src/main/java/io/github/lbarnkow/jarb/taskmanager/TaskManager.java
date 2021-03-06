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

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;

import io.github.lbarnkow.jarb.taskmanager.managementtasks.DeadTaskPrunerTask;
import io.github.lbarnkow.jarb.taskmanager.managementtasks.TaskTableLoggerTask;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * The <code>TaskManager</code> collects multiple tasks and controls their
 * life-cycle. It uses <code>TaskWrapper</code> to spawn a thread per task.
 * Furthermore, it starts additional management tasks for house-keeping (e.g. to
 * automatically prune DEAD tasks from the list of managed tasks).
 *
 * @author lbarnkow
 */
@ToString
@NoArgsConstructor
@Slf4j
public class TaskManager {
  /**
   * A flag indicating whether this instance has started its management tasks or
   * not.
   */
  private boolean started;

  /**
   * The collection <code>Task</code>s and associated <code>TaskWrapper</code>s
   * managed by this instance.
   */
  private final Map<Task, TaskWrapper> tasks = new ConcurrentHashMap<>();

  /**
   * The management tasks employed by this instance.
   */
  private final Task[] managementTasks =
      new Task[] { new TaskTableLoggerTask(this), new DeadTaskPrunerTask(this) };

  /**
   * Spawns threads for a set of given tasks and monitors their progress in the
   * task lifecycle.
   *
   * @param callback a function to call upon each tasks termination
   * @param tasks    the tasks to run and track
   */
  @Synchronized
  public void start(Optional<TaskEndedCallback> callback, Task... tasks) {
    start(callback, false, tasks);
  }

  /**
   * Spawns threads for a set of given tasks and monitors their progress in the
   * task lifecycle.
   *
   * @param callback               a function to call upon each tasks termination
   * @param disableManagementTasks allows to disable managements tasks, for
   *                               example automatically pruning DEAD tasks
   * @param tasks                  the tasks to run and track
   */
  @Synchronized
  public void start(Optional<TaskEndedCallback> callback, boolean disableManagementTasks,
      Task... tasks) {
    if (!started) {
      if (!disableManagementTasks) {
        startTasks(callback, managementTasks);
      }
      started = true;
    }

    startTasks(callback, tasks);
  }

  private void startTasks(Optional<TaskEndedCallback> callback, Task... tasks) {
    for (Task task : tasks) {
      @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // unique objects needed
      TaskWrapper wrapper = new TaskWrapper(task);
      wrapper.startTask(callback);
      this.tasks.put(task, wrapper);
    }
  }

  /**
   * Signals this instance to stop all managed tasks (including management tasks).
   * This method blocks until all tasks have reached the state 'DEAD'.
   */
  @Synchronized
  public void stopAll() {
    tasks.keySet().stream().forEach(task -> stop(task));
    waitForAllTasksToFinish();
  }

  /**
   * Signals a set of given tasks (which have to be started and managed by this
   * instance) to stop. Note that this method does not wait for the tasks to
   * actually terminate.
   *
   * @param tasks the tasks to stop
   */
  public void stop(Task... tasks) {
    for (Task task : tasks) {
      this.tasks.get(task).stopTask();
    }
  }

  /**
   * Stops tracking a set of given tasks (which have to be started and managed by
   * this instance), thereby removing them from the internal list of managed
   * tasks. Note that these tasks must already have terminated!
   *
   * @param tasks the terminated tasks to prune
   */
  public void prune(Task... tasks) {
    for (Task task : tasks) {
      TaskWrapper wrapper = this.tasks.get(task);
      if (wrapper.getState() != DEAD) {
        throw new IllegalStateException("Only tasks in state " + DEAD + " can be pruned!");
      }
      this.tasks.remove(task);
    }
  }

  /**
   * Gets the number of managed tasks (including management tasks).
   *
   * @return the number of managed tasks
   */
  public int getTaskCount() {
    return tasks.size();
  }

  /**
   * Gets the number of management tasks.
   *
   * @return the number of management tasks.
   */
  public int getNumberOfManagementTasks() {
    return managementTasks.length;
  }

  /**
   * Gets the tasks managed by this instance (including management tasks).
   *
   * @return the tasks
   */
  public Set<Task> getTasks() {
    return new HashSet<>(tasks.keySet());
  }

  /**
   * Gets the <code>TaskState</code> for a given <code>Task</code>.
   *
   * @param task the <code>Task</code>
   * @return the <code>TaskState</code>
   */
  public TaskState getTaskState(Task task) {
    return tasks.get(task).getState();
  }

  private void waitForAllTasksToFinish() {
    try {
      boolean done = false;
      while (!done) {
        done = true;
        for (TaskWrapper wrapper : tasks.values()) {
          if (wrapper.getState() != DEAD) {
            done = false;
          }
        }
        Thread.sleep(50L);
      }

    } catch (InterruptedException e) {
      log.error("Caught InterruptedException while waiting for all tasks to stop!");
    }
  }
}
