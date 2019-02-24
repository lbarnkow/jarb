package bot;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main extends CommonBase {
	@Inject
	private ConnectionInfo conInfo;
	@Inject
	private LinkBot bot;
	@Inject
	private Runtime runtime;

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String... args) throws IOException {
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
				System.out.println("JVM is shutting down, stopping all thread/services...");
				bot.stop();
				System.out.println("All threads/service stopped.");
			}
		});

		bot.run();
	}
}
