package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import io.github.lbarnkow.rocketbot.misc.Common;

@SuppressWarnings("unused")
public class SendLogin extends BaseMessageWithMethod {
	private static final String METHOD = "login";

	private Params[] params;

	public SendLogin(String username, String password) {
		super(METHOD);
		this.params = new Params[] { new Params(username, password) };
	}

	private static class Params extends Common {
		private final User user;
		private final Password password;

		public Params(String username, String passwordHash) {
			this.user = new User(username);
			this.password = new Password(passwordHash);
		}
	}

	private static class User extends Common {
		private final String username;

		public User(String username) {
			this.username = username;
		}
	}

	private static class Password extends Common {
		private final String digest;
		private final String algorithm = "sha-256";

		public Password(String passwordHash) {
			this.digest = passwordHash;
		}
	}
}
