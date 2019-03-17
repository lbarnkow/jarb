package io.github.lbarnkow.rocketbot.api;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.misc.Common;
import io.github.lbarnkow.rocketbot.misc.Holder;

public abstract class Bot {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final Holder<AuthInfo> authHolder = new Holder<>(AuthInfo.INVALID);

	public abstract void initialize();

	public abstract Credentials getCredentials();

	public String getName() {
		return getClass().getSimpleName();
	}

	public boolean getAutojoinPublicChannels() {
		return true;
	}

	public abstract void offerMessage(Message message);

	public Holder<AuthInfo> getAuthHolder() {
		return authHolder;
	}

	public static class AuthInfo extends Common {
		static final AuthInfo INVALID = new AuthInfo(null, null, Instant.ofEpochMilli(0));

		private final String userId;
		private final String authToken;
		private final Instant expires;

		public AuthInfo(String userId, String authToken, Instant expires) {
			this.userId = userId;
			this.authToken = authToken;
			this.expires = expires;
		}

		public String getUserId() {
			return userId;
		}

		public String getAuthToken() {
			return authToken;
		}

		public Instant getExpires() {
			return expires;
		}

		public boolean isValid() {
			return expires.isAfter(Instant.now());
		}
	}

	public static class Credentials {
		private final String username;
		private final String passwordHash;

		public Credentials(String username, String passwordHash) {
			this.username = username;
			this.passwordHash = passwordHash;
		}

		public String getUsername() {
			return username;
		}

		public String getPasswordHash() {
			return passwordHash;
		}
	}
}
