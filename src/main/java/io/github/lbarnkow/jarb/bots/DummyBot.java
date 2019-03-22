package io.github.lbarnkow.jarb.bots;

import java.util.Optional;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;

public class DummyBot extends Bot {
	private Credentials credentials;
	private String name = getClass().getSimpleName();

	public DummyBot() {
	}

	@Override
	public Bot initialize(String name, Credentials credentials) {
		this.name = name;
		this.credentials = credentials;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Credentials getCredentials() {
		return credentials;
	}

	@Override
	public Optional<Message> offerMessage(Message message) {
		logger.error(message.toString());
		return Optional.empty();
	}

	@Override
	public boolean offerRoom(Room room) {
		return true;
	}
}