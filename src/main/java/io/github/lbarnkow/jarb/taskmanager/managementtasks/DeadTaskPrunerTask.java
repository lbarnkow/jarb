package io.github.lbarnkow.jarb.taskmanager.managementtasks;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;

import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import io.github.lbarnkow.jarb.taskmanager.Task;
import io.github.lbarnkow.jarb.taskmanager.TaskManager;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
@AllArgsConstructor
public class DeadTaskPrunerTask extends AbstractBaseTask {
  private static final Logger logger = LoggerFactory.getLogger(DeadTaskPrunerTask.class);

  private static final long TASK_INTERVAL_MSEC = 1000L * 60L * 5L; // repeat TASK every 5 minutes

  @ToString.Exclude
  private final TaskManager manager;
  private final long taskInterval;

  public DeadTaskPrunerTask(TaskManager manager) {
    this(manager, TASK_INTERVAL_MSEC);
  }

  @Override
  public void runTask() throws Throwable {
    while (true) {
      pruneTasks();

      try {
        Thread.sleep(taskInterval);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  int pruneTasks() {
    int pruned = 0;

    for (Task task : manager.getTasks()) {
      if (manager.getTaskState(task) == DEAD) {
        logger.debug("Pruning task '{}' in state '{}'.", task.getName(), DEAD);
        pruned++;
        manager.prune(task);
      }
    }

    if (pruned > 0) {
      logger.info("Pruned {} background tasks in state '{}'.", pruned, DEAD);
    }

    return pruned;
  }
}
