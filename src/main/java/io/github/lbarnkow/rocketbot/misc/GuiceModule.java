package io.github.lbarnkow.rocketbot.misc;

import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {
	@Override
	protected void configure() {
		super.configure();

		bind(Runtime.class).toInstance(Runtime.getRuntime());
//		bind(Interface.class).to(Implementation.class);
	}
}
