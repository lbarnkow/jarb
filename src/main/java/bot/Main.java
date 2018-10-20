package bot;

public class Main {
	public static void main(String... args) {
		String url = "ws://rocket.system.local/websocket/";
		String username = "admin";
		String password = "qqSKfA1hH9n37uR979iuck7POImY3HZp";

		ConnectionInfo conInfo = new ConnectionInfo(url, username, password);
		final Bot bot = new Bot(conInfo);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				bot.stop();
			}
		});

		Thread t = new Thread(bot);
		t.start();
	}
}
