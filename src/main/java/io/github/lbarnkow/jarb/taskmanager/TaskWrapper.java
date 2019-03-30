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

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;

import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class TaskWrapper {
  private final Task task;
  private Thread thread;
  private TaskState state = UNUSED;
  private Throwable lastError;

  /**
   * Starts the thread on which the tasks work is being processed.
   *
   * @param callback a function to call upon the tasks termination
   */
  public void startTask(Optional<TaskEndedCallback> callback) {
    if (state != UNUSED) {
      throw new IllegalStateException("Tasks can only be started once!");
    }

    Optional<TaskEndedCallback> cb = callback != null ? callback : Optional.empty();

    state = ACTIVATING;
    thread = new Thread(() -> executeTask(cb), task.getName());
    thread.start();
  }

  private void executeTask(Optional<TaskEndedCallback> callback) {
    try {
      task.initializeTask();
    } catch (Throwable t) {
      handleError("Initialization", t, callback);
      return;
    }

    state = ACTIVE;
    try {
      task.runTask();
    } catch (Throwable t) {
      handleError("Execution", t, callback);
      return;
    }

    state = DEAD;
    log.info("Task '{}' finished.", task.getName());
    if (callback.isPresent()) {
      TaskEndedEvent event =
          TaskEndedEvent.builder().task(task).state(state).lastError(Optional.empty()).build();
      callback.get().onTaskEnded(event);
    }
  }

  /**
   * Signals a running task to stop its work immediately
   * (<code>Thread.interrupt()</code>).
   */
  public void stopTask() {
    if (state == TaskState.ACTIVATING || state == TaskState.ACTIVE) {
      state = DEACTIVATING;
      thread.interrupt();
    }
  }

  private void handleError(String stage, Throwable t, Optional<TaskEndedCallback> callback) {
    lastError = t;
    log.error("{} of task '{}' raised an unexpected exception!", stage, task.getName(), t);
    state = DEAD;
    if (callback.isPresent()) {
      TaskEndedEvent event =
          TaskEndedEvent.builder().task(task).state(state).lastError(Optional.of(t)).build();
      callback.get().onTaskEnded(event);
    }
  }
}
