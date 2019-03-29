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

@Data
@JarbJsonSettings
//@JsonIgnoreProperties({ "websocketUrl", "restUrl" })
public class ConnectionConfiguration {
  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;
  private static final boolean DEFAULT_IS_ENCRYPTED = false;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private boolean encrypted = DEFAULT_IS_ENCRYPTED;

  public String getWebsocketUrl() {
    String scheme = encrypted ? "wss" : "ws";
    return scheme + "://" + host + ":" + port + "/websocket/";
  }

  public String getRestUrl() {
    String scheme = encrypted ? "https" : "http";
    return scheme + "://" + host + ":" + port + "/api/v1";
  }
}
