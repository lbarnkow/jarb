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

/**
 * A background task.
 *
 * @author lbarnkow
 */
public interface Task {
  /**
   * Gets the logical name of this <code>Task</code>.
   *
   * @return the logical name
   */
  String getName();

  /**
   * Handles any work necessary to initialize the backround tasks.
   *
   * @throws Throwable on errors
   */
  void initializeTask() throws Throwable;

  /**
   * Handles the main workload of the background task. This method <b>MUST</b>
   * return control to the caller when an <code>InterruptedException</code>
   * occurs!
   *
   * @throws Throwable on errors
   */
  void runTask() throws Throwable;
}
