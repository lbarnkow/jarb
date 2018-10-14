package messages.requests;

import org.apache.commons.codec.digest.DigestUtils;

public class ReqLogin extends ReqMethodBase {
	private final Params[] params;

	public ReqLogin(String username, String password) {
		super("login");
		this.params = new Params[] { new Params(username, password) };
	}

	public Params[] getParams() {
		return params;
	}

	public static class Params {
		private final User user;
		private final Password password;

		public Params(String username, String password) {
			this.user = new User(username);
			this.password = new Password(password);
		}

		public User getUser() {
			return user;
		}

		public Password getPassword() {
			return password;
		}
	}

	public static class User {
		private final String username;

		public User(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
		}
	}

	public static class Password {
		private final String digest;
		private final String algorithm = "sha-256";

		public Password(String password) {
			this.digest = DigestUtils.sha256Hex(password);
		}

		public String getDigest() {
			return digest;
		}

		public String getAlgorithm() {
			return algorithm;
		}
	}
}
