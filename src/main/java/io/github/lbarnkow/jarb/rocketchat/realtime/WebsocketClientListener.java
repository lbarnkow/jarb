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

package io.github.lbarnkow.jarb.rocketchat.realtime;

/**
 * A listener that is to be informed on all changes regarding the websocket
 * connection to the chat server.
 * 
 * @author lbarnkow
 */
public interface WebsocketClientListener {
  /**
   * Occurs upon websocket connection termination.
   * 
   * @param initiatedByClient <code>true</code> if the termination was initiated
   *                          by the client; <code>false</code> otherwise
   */
  void onWebsocketClose(boolean initiatedByClient);

  /**
   * Occurs upon receiving a message from the chat server through the websocket.
   * 
   * @param message the message
   */
  void onWebsocketMessage(String message);
}
