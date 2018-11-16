package bot;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main extends CommonBase {
	@Inject
	private ConnectionInfo conInfo;
	@Inject
	private Bot bot;
	@Inject
	private Runtime runtime;

	public static void main(String... args) {
//		String hostname = "rockettest.system.local";
//		int port = 80;
		String hostname = "localhost";
		int port = 8080;

		String username = "demobot";
		String password = "demobot";

		Injector guice = Guice.createInjector(new BotModule());

		Main instance = guice.getInstance(Main.class);
		instance.main(hostname, port, username, password);
	}

	public void main(String hostname, int port, String username, String password) {
		conInfo.initialize(false, hostname, port, username, password);

		runtime.addShutdownHook(new Thread() {
			@Override
			public void run() {
				bot.stop();
			}
		});

		bot.run();
	}
}
