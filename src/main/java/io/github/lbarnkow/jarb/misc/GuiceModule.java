package io.github.lbarnkow.jarb.misc;

import javax.inject.Provider;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.google.inject.AbstractModule;

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
