package bot.rocketchat.websocket.messages;

import org.apache.commons.codec.digest.DigestUtils;

public class SendLogin extends SendMethod {
	private static final String METHOD = "login";

	@SuppressWarnings("unused")
	private final Params[] params;

	public SendLogin(String id, String username, String password) {
		super(id, METHOD);
		this.params = new Params[] { new Params(username, password) };
	}

	private static class Params {
		@SuppressWarnings("unused")
		private final User user;
		@SuppressWarnings("unused")
		private final Password password;

		public Params(String username, String password) {
			this.user = new User(username);
			this.password = new Password(password);
		}
	}

	private static class User {
		@SuppressWarnings("unused")
		private final String username;

		public User(String username) {
			this.username = username;
		}
	}

	private static class Password {
		@SuppressWarnings("unused")
		private final String digest;
		@SuppressWarnings("unused")
		private final String algorithm = "sha-256";

		public Password(String password) {
			this.digest = DigestUtils.sha256Hex(password);
		}
	}
}
