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

import io.github.lbarnkow.jarb.api.Bot;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract base class extending <code>AbstractBaseTask</code> storing a
 * <code>Bot</code> instance. It also names the task extending this class after
 * the bot it encapsulates.
 *
 * @author lbarnkow
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBotSpecificTask extends AbstractBaseTask {

  /**
   * The <code>Bot</code> associated with this <code>Task</code>.
   */
  @Getter
  private final Bot bot;

  /**
   * Construct a new instance associated with a given <code>Bot</code>.
   *
   * @param bot the <code>Bot</code>
   */
  public AbstractBotSpecificTask(final Bot bot) {
    this.bot = bot;
    this.setName(getClass().getSimpleName() + "-" + bot.getName() + "-thread");
  }
}
