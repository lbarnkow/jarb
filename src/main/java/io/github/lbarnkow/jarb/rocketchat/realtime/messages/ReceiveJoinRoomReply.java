package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveJoinRoomReply extends BaseMessage {
  private boolean result;

  public boolean isSuccess() {
    return result;
  }
}
