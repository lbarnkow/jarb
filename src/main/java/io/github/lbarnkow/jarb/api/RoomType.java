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

/**
 * Enumeration of room types (e.g. public channel, private channel, direct
 * messages, …).
 *
 * @author lbarnkow
 */
public enum RoomType {
  PUBLIC_CHANNEL("c"), //
  PRIVATE_GROUP("g"), //
  INSTANT_MESSAGE("d"), //
  LIVE_CHAT("l"); //

  private final String rawType;

  private RoomType(String rawType) {
    this.rawType = rawType;
  }

  public String getRawType() {
    return rawType;
  }

  /**
   * Parses a raw room type from Rocket.Chat and converts it into the appropriate
   * enum value.
   *
   * @param rawType the raw room type
   * @return a enum value
   */
  public static RoomType parse(String rawType) {
    for (RoomType type : RoomType.values()) {
      if (type.rawType.equals(rawType)) {
        return type;
      }
    }

    throw new IllegalArgumentException(
        "Parameter rawType '" + rawType + "' doesn't map to any known RoomType instance!");
  }
}
