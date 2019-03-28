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
