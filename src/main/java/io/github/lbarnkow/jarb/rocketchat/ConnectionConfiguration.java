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

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

/**
 * Chat server connection configuration loaded from YAML.
 *
 * @author lbarnkow
 */
@Data
@JarbJsonSettings
public class ConnectionConfiguration {
  /**
   * The default host name to use as chat server.
   */
  private static final String DEFAULT_HOST = "localhost";

  /**
   * The default port to connect to.
   */
  private static final int DEFAULT_PORT = 8080;

  /**
   * The default value to indicate use of encrypted connections.
   */
  private static final boolean DEFAULT_IS_ENCRYPTED = false;

  /**
   * The host name to use as chat server.
   */
  private String host = DEFAULT_HOST;

  /**
   * The port to connect to.
   */
  private int port = DEFAULT_PORT;

  /**
   * A falg indicating whether encrypted connections should be used or not.
   */
  private boolean encrypted = DEFAULT_IS_ENCRYPTED;

  /**
   * Returns the URL for websocket connections to the chat server.
   *
   * @return the websocket URL
   */
  public String getWebsocketUrl() {
    String scheme = encrypted ? "wss" : "ws";
    return scheme + "://" + host + ":" + port + "/websocket/";
  }

  /**
   * Returns the URL for REST calls to the chat server.
   *
   * @return the REST URL
   */
  public String getRestUrl() {
    String scheme = encrypted ? "https" : "http";
    return scheme + "://" + host + ":" + port + "/api/v1";
  }
}
