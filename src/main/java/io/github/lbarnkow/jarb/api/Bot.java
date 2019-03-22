package io.github.lbarnkow.jarb.api;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Bot {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public abstract Bot initialize(String name, Credentials credentials);

	public abstract Credentials getCredentials();

	public String getName() {
		return getClass().getSimpleName();
	}

	public abstract boolean offerRoom(Room room);

	public abstract Optional<Message> offerMessage(Message message);
}
