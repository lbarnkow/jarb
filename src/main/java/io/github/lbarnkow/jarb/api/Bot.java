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

package io.github.lbarnkow.jarb.api;

import java.util.Optional;

/**
 * Every bot handled by jarb must implement this interface.
 *
 * @author lbarnkow
 */
@SuppressWarnings("PMD.ShortClassName")
public interface Bot {
  /**
   * Called by jarb to initialize the <code>Bot</code> instance with its logical
   * name within the jarb run-time, as well as, its user name on the chat server.
   *
   * @param name     the logical <code>Bot</code> name within the jarb run-time
   * @param username the user name on the chat server
   * @return this instance
   */
  Bot initialize(String name, String username);

  /**
   * Gets the logical name within the jarb run-time.
   *
   * @return the logical name within the jarb run-time
   */
  String getName();

  /**
   * Gets the user name on the chat server.
   *
   * @return the user name on the chat server.
   */
  String getUsername();

  /**
   * Called by jarb to offer a <code>Bot</code> to join a (new) <code>Room</code>
   * it hasn't yet subscribed to.
   *
   * @param room the <code>Room</code>
   * @return <code>true</code> if the <code>Bot</code> wishes to join;
   *         <code>false</code> otherwise
   */
  boolean offerRoom(Room room);

  /**
   * Called by jarb to offer a <code>Message</code> received from one of the
   * <code>Room</code>s this <code>Bot</code> has joined.
   *
   * @param message the <code>Message</code> received
   * @return an optional <code>Message</code> this <code>Bot</code> wishes to send
   *         as an answer
   */
  Optional<Message> offerMessage(Message message);
}
