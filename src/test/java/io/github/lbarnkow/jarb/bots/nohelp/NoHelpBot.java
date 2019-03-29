package io.github.lbarnkow.jarb.bots.nohelp;

import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.bots.AbstractBaseBot;
import java.util.Optional;

public class NoHelpBot extends AbstractBaseBot {
  @Override
  public boolean offerRoom(Room room) {
    return false;
  }

  @Override
  public Optional<Message> offerMessage(Message message) {
    return Optional.empty();
  }

}
