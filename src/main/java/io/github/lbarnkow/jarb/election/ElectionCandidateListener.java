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

package io.github.lbarnkow.jarb.election;

/**
 * Listens to state changes of candidates in the election process / life-cycle.
 *
 * @author lbarnkow
 */
public interface ElectionCandidateListener {
  /**
   * Called by the <code>ElectionCandidate</code> when ever its
   * <code>ElectionCandidateState</code> changes.
   *
   * @param source   the <code>ElectionCandidate</code> emitting the event
   * @param oldState the old state before the change
   * @param newState the new state after the change
   */
  void onStateChanged(ElectionCandidate source, ElectionCandidateState oldState,
      ElectionCandidateState newState);
}
