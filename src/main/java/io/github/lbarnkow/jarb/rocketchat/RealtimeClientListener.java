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

package io.github.lbarnkow.jarb.rocketchat;

/**
 * A listener that is to be informed on events concerning the real-time (web
 * socket) session with the chat server.
 *
 * @author lbarnkow
 */
public interface RealtimeClientListener {
  /**
   * Called when a real-time session is established.
   * 
   * @param source the <code>RealtimeClient</code> emitting this event
   */
  void onRealtimeClientSessionEstablished(RealtimeClient source);

  /**
   * Called when the real-time session is closed.
   * 
   * @param source            the <code>RealtimeClient</code> emitting this event
   * @param initiatedByClient indicates whether the shutdown was initiated by the
   *                          client or the chat server
   */
  void onRealtimeClientSessionClose(RealtimeClient source, boolean initiatedByClient);

  /**
   * Called when a "stream-room-messages" subscription received an update.
   * 
   * @param source the <code>RealtimeClient</code> emitting this event
   * @param roomId the room id for which a new message was received
   */
  void onRealtimeClientStreamRoomMessagesUpdate(RealtimeClient source, String roomId);
}
