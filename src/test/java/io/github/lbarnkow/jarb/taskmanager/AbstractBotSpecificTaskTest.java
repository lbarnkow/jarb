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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.api.Bot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractBotSpecificTaskTest {

  @Test
  void test() {
    // given
    Bot bot = Mockito.mock(Bot.class);
    when(bot.getName()).thenReturn("TestBot");

    // when
    AbstractBotSpecificTaskTestImpl task = new AbstractBotSpecificTaskTestImpl(bot);

    // then
    assertThat(task.getBot()).isSameAs(bot);
    assertThat(task.getName()).contains(bot.getName());
  }

  private static class AbstractBotSpecificTaskTestImpl extends AbstractBotSpecificTask {
    public AbstractBotSpecificTaskTestImpl(Bot bot) {
      super(bot);
    }

    @Override
    public void runTask() throws Throwable {
    }

  }
}
