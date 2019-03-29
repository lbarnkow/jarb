package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import io.github.lbarnkow.jarb.taskmanager.managementtasks.DeadTaskPrunerTask;
import io.github.lbarnkow.jarb.taskmanager.managementtasks.TaskTableLoggerTask;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Synchronized;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
public class TaskManager {
  private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

  private boolean started = false;
  private final Map<Task, TaskWrapper> tasks = new ConcurrentHashMap<>();

  private final Task[] managementTasks =
      new Task[] { new TaskTableLoggerTask(this), new DeadTaskPrunerTask(this) };

  public TaskManager() {
  }

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
      TaskWrapper wrapper = new TaskWrapper(task);
      wrapper.startTask(callback);
      this.tasks.put(task, wrapper);
    }
  }

  public synchronized void stopAll() {
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

  public int getTaskCount() {
    return tasks.size();
  }

  public int getNumberOfManagementTasks() {
    return managementTasks.length;
  }

  public Set<Task> getTasks() {
    return new HashSet<>(tasks.keySet());
  }

  public TaskState getTaskState(Task task) {
    return tasks.get(task).getState();
  }

  private void waitForAllTasksToFinish() {
    try {
      boolean done = false;
      while (!done) {
        done = true;
        for (TaskWrapper wrapper : tasks.values()) {
          if (wrapper.getState() != UNUSED && wrapper.getState() != DEAD) {
            done = false;
          }
        }
        Thread.sleep(50L);
      }

    } catch (InterruptedException e) {
      logger.error("Caught InterruptedException while waiting for all tasks to stop!");
    }
  }
}
