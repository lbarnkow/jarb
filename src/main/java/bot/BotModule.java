package bot;

import com.google.inject.AbstractModule;

public class BotModule extends AbstractModule {
	@Override
	protected void configure() {
		super.configure();

		bind(Runtime.class).toInstance(Runtime.getRuntime());
//		bind(Interface.class).to(Implementation.class);
	}
}
