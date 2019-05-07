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

package io.github.lbarnkow.jarb.bots.dummy;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.bots.AbstractBaseBot;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Test bot that joins every public channel and dumps every encountered message
 * to the log.
 *
 * @author lbarnkow
 */
@Slf4j
public class DummyBot extends AbstractBaseBot implements Bot {

  @Override
  public boolean offerRoom(final Room room) {
    return true;
  }

  @Override
  public Optional<Message> offerMessage(final Message message) {
    log.error(message.toString());
    return Optional.empty();
  }
}