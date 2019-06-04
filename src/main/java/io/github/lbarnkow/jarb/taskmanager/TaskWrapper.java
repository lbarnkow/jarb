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

/**
 * Wrapper around a <code>Task</code> that handles a tasks life-cycle, from
 * being <code>UNUSED</code> to its termination in state <code>DEAD</code>. The
 * wrapper spawns a background thread on with the tasks code is executed. It
 * catches and handles uncaught exception occurring within the tasks code to
 * make sure the background threads shutdown in a somewhat controlled manner.
 * Optionally, a listener can be informed about the end of the task life-cycle.
 *
 * @author lbarnkow
 */
@Data
@Slf4j
public class TaskWrapper {
  /**
   * The <code>Task</code> wrapped by this instance.
   */
  private final transient Task task;

  /**
   * The <code>Thread</code> running the wrapped tasks work load.
   */
  private transient Thread thread;

  /**
   * The state of the task life-cycle this instance is currently in.
   */
  private transient TaskState state = UNUSED;

  /**
   * The last Exception thrown by the wrapped task.
   */
  private transient Throwable lastError;

  /**
   * Starts the thread on which the tasks work is being processed.
   *
   * @param callback a function to call upon the tasks termination
   */
  public void startTask(final Optional<TaskEndedCallback> callback) {
    if (state != UNUSED) {
      throw new IllegalStateException("Tasks can only be started once!");
    }

    final Optional<TaskEndedCallback> cb = callback != null ? callback : Optional.empty();

    state = ACTIVATING;
    thread = new Thread(() -> executeTask(cb), task.getName());
    thread.start();
  }

  private void executeTask(final Optional<TaskEndedCallback> callback) {
    try {
      task.initializeTask();
    } catch (final Exception t) {
      handleError("Initialization", t, callback);
      return;
    }

    state = ACTIVE;
    try {
      task.runTask();
    } catch (final Exception t) {
      handleError("Execution", t, callback);
      return;
    }

    state = DEAD;
    log.info("Task '{}' finished.", task.getName());
    if (callback.isPresent()) {
      final TaskEndedEvent event =
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

  private void handleError(final String stage, final Throwable t,
      final Optional<TaskEndedCallback> callback) {
    lastError = t;
    log.error("{} of task '{}' raised an unexpected exception!", stage, task.getName(), t);
    state = DEAD;
    if (callback.isPresent()) {
      final TaskEndedEvent event =
          TaskEndedEvent.builder().task(task).state(state).lastError(Optional.of(t)).build();
      callback.get().onTaskEnded(event);
    }
  }
}
