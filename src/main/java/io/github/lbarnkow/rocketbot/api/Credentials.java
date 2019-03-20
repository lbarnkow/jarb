package io.github.lbarnkow.rocketbot.api;

public class Credentials {
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