package io.github.lbarnkow.jarb.api;

import java.time.Instant;

import io.github.lbarnkow.jarb.misc.Common;

public class AuthInfo extends Common {
	public static final AuthInfo INVALID = new AuthInfo(null, null, Instant.ofEpochMilli(0));

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