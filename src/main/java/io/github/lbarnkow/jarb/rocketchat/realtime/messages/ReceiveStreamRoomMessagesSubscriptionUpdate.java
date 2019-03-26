package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveStreamRoomMessagesSubscriptionUpdate extends BaseMessage {
  private Fields fields;

  @JarbJsonSettings
  @Data
  public static class Fields {
    private List<Arg> args;
  }

  @JarbJsonSettings
  @Data
  public static class Arg {
    private String rid;
  }
}
