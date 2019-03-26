package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveLoginReply extends BaseMessage {
  private LoginResult result;

  @JarbJsonSettings
  @Data
  public static class LoginResult {
    private String id;
    private String token;
    private RawDate tokenExpires;
    private String type;
  }
}
