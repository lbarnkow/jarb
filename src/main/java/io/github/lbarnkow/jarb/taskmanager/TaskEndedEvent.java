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

import java.util.Optional;

import lombok.Builder;
import lombok.Value;

/**
 * An event produced by <code>TaskWrapper</code> upon termination of a task.
 * Usually, this event will be sent to <code>TaskManager</code> and from there
 * relayed to the party initially supplying the task.
 *
 * @author lbarnkow
 */
@Value
@Builder
public class TaskEndedEvent {
  private Task task;
  private TaskState state;
  private Optional<Throwable> lastError;
}
