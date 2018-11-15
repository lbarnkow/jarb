package bot;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main extends CommonBase {
	public static void main(String... args) {
//		String hostname = "rockettest.system.local";
//		int port = 80;
		String hostname = "localhost";
		int port = 8080;

		String username = "demobot";
		String password = "demobot";

		Injector guice = Guice.createInjector(new BotModule());

		ConnectionInfo conInfoSingleton = guice.getInstance(ConnectionInfo.class);
		conInfoSingleton.initialize(false, hostname, port, username, password);

		final Bot bot = guice.getInstance(Bot.class);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				bot.stop();
			}
		});

		bot.run();
	}
}
