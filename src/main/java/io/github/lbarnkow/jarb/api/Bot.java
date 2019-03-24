package io.github.lbarnkow.jarb.api;

import java.util.Optional;

public interface Bot {
	Bot initialize(String name, Credentials credentials);

	String getName();

	Credentials getCredentials();

	boolean offerRoom(Room room);

	Optional<Message> offerMessage(Message message);
}
