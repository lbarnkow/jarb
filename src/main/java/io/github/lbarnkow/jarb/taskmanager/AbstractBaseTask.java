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

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base implementation of interface <code>Task</code> that
 * automatically determines the tasks name based on the name of the class
 * extending this base class.
 *
 * @author lbarnkow
 */
public abstract class AbstractBaseTask implements Task {
  /**
   * The logical name for this <code>Task</code>. Defaults to the class name
   * suffixed by "-thread".
   */
  @Getter
  @Setter
  private String name = getClass().getSimpleName() + "-thread";

  @Override
  @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
  public void initializeTask() throws Exception {
    // A lot of tasks don't need to do initialization. This empty method body
    // reduces the clutter in sub classes.
  }
}
