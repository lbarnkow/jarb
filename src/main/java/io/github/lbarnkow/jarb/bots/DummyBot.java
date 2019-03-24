package io.github.lbarnkow.jarb.bots;

import java.util.Optional;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DummyBot extends AbstractBaseBot implements Bot {
	public DummyBot() {
	}

	@Override
	public boolean offerRoom(Room room) {
		return true;
	}

	@Override
	public Optional<Message> offerMessage(Message message) {
		log.error(message.toString());
		return Optional.empty();
	}
}