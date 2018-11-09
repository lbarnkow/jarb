package bot;

public class Main extends CommonBase {
	public static void main(String... args) {
//		String hostname = "rockettest.system.local";
//		int port = 80;
		String hostname = "localhost";
		int port = 8080;

		String username = "demobot";
		String password = "demobot";

		ConnectionInfo conInfo = new ConnectionInfo(false, hostname, port, username, password);
		final Bot bot = new Bot(conInfo);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				bot.stop();
			}
		});

		Thread t = new Thread(bot);
		t.start();

		// TODO: raus?!
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
}
