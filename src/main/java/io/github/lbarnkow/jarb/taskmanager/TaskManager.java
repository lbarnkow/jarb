package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import io.github.lbarnkow.jarb.taskmanager.managementtasks.DeadTaskPrunerTask;
import io.github.lbarnkow.jarb.taskmanager.managementtasks.TaskTableLoggerTask;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Synchronized;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
public class TaskManager {
  private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

  private boolean started = false;
  private final Map<Task, TaskWrapper> tasks = new HashMap<>();

  private final Task[] managementTasks =
      new Task[] { new TaskTableLoggerTask(this), new DeadTaskPrunerTask(this) };

  public TaskManager() {
  }

  @Synchronized
  public void start(Optional<TaskEndedCallback> callback, Task... tasks) {
    // TODO: Allow managementTasks to be deactivated?
    if (!started) {
      startTasks(callback, managementTasks);
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

  public void stop(Task... tasks) {
    for (Task task : tasks) {
      this.tasks.get(task).stopTask();
    }
  }

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
