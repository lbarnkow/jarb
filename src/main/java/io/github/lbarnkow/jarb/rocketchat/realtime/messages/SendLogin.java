package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendLogin extends BaseMessageWithMethod {
	private static final String METHOD = "login";

	private Params[] params;

	public SendLogin(String username, String password) {
		super(METHOD);
		this.params = new Params[] { new Params(username, password) };
	}

	@JarbJsonSettings
	@Data
	private static class Params {
		private final User user;
		private final Password password;

		public Params(String username, String password) {
			this.user = new User(username);
			this.password = new Password(password);
		}
	}

	@JarbJsonSettings
	@Data
	private static class User {
		private final String username;
	}

	@JarbJsonSettings
	@Data
	private static class Password {
		private final String digest;
		private final String algorithm = "sha-256";
	}
}
