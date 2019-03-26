package io.github.lbarnkow.jarb.rocketchat.realtime;

import io.github.lbarnkow.jarb.rocketchat.realtime.messages.BaseMessage;
import lombok.Getter;
import lombok.ToString;

@ToString
public class ReplyErrorException extends Exception {
  private static final long serialVersionUID = -1709873181129875291L;

  @Getter
  private final BaseMessage.Error error;

  public ReplyErrorException(BaseMessage.Error error) {
    this.error = error;
  }
}
