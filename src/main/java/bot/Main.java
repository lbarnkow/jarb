package bot;

public class Main {
	public static void main(String... args) {
		final Bot bot = new Bot("uri", "username", "password");

		bot.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				bot.shutdown();
			}
		});
	}

	public static class Bot {

		public Bot(String string, String string2, String string3) {
		}

		public void start() {
			// connect websocket to setup incoming message handler
			// send connect and wait for connected reply (w/ session id)
			// send login and wait for reply w/ same id (w/ auth-info: userid, token,
			// tokenExpires)

			// NOTE: Login token can be renewed, before the token expires!
		}

		public void shutdown() {
		}
	}
}
