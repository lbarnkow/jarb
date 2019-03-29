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

package io.github.lbarnkow.jarb.misc;

import com.google.inject.AbstractModule;
import javax.inject.Provider;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import lombok.Generated;

// To exclude this class from code coverage. I know, it's wrong, but jacoco currently doesn't offer
// anything better. :(
@Generated
public class GuiceModule extends AbstractModule implements Provider<Client> {
  @Override
  protected void configure() {
    super.configure();

    bind(Runtime.class).toInstance(Runtime.getRuntime());
    bind(WebSocketContainer.class).toInstance(ContainerProvider.getWebSocketContainer());
    bind(Client.class).toProvider(this);
  }

  @Override
  public Client get() {
    return ClientBuilder.newClient();
  }
}
