package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendLogin extends BaseMessageWithMethod {
	private static final String METHOD = "login";

	private Params[] params;

	public SendLogin(String username, String password) {
		super(METHOD);
		this.params = new Params[] { new Params(username, password) };
	}

	@MyJsonSettings
	@Data
	private static class Params {
		private final User user;
		private final Password password;

		public Params(String username, String passwordHash) {
			this.user = new User(username);
			this.password = new Password(passwordHash);
		}
	}

	@MyJsonSettings
	@Data
	private static class User {
		private final String username;
	}

	@MyJsonSettings
	@Data
	private static class Password {
		private final String digest;
		private final String algorithm = "sha-256";
	}
}
