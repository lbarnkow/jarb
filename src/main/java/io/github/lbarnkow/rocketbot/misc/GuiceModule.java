package io.github.lbarnkow.rocketbot.misc;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {
	@Override
	protected void configure() {
		super.configure();

		bind(Runtime.class).toInstance(Runtime.getRuntime());
		bind(WebSocketContainer.class).toInstance(ContainerProvider.getWebSocketContainer());
	}
}
