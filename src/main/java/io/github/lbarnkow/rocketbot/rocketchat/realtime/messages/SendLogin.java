package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

import org.apache.commons.codec.digest.DigestUtils;

import io.github.lbarnkow.rocketbot.misc.Common;

@SuppressWarnings("unused")
public class SendLogin extends BaseWithMethod {
	private static final String METHOD = "login";

	private Params[] params;

	public SendLogin(String username, String password) {
		super(METHOD);
		this.params = new Params[] { new Params(username, password) };
	}

//	public Params[] getParams() {
//		return params;
//	}

	private static class Params extends Common {
		private final User user;
		private final Password password;

		public Params(String username, String password) {
			this.user = new User(username);
			this.password = new Password(password);
		}

//		public User getUser() {
//			return user;
//		}
//
//		public Password getPassword() {
//			return password;
//		}
	}

	private static class User extends Common {
		private final String username;

		public User(String username) {
			this.username = username;
		}

//		public String getUsername() {
//			return username;
//		}
	}

	private static class Password extends Common {
		private final String digest;
		private final String algorithm = "sha-256";

		public Password(String password) {
			// TODO: don't hash here. Instead, use hashed passwords in the configuration
			// file!
			this.digest = DigestUtils.sha256Hex(password);
		}

//		public String getDigest() {
//			return digest;
//		}
//
//		public String getAlgorithm() {
//			return algorithm;
//		}
	}
}
