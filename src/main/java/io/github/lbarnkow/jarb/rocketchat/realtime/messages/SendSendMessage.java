package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Message;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSendMessage extends BaseMessageWithMethod {
  private static final String METHOD = "sendMessage";

  private List<RawMessage> params;

  public SendSendMessage(Message message) {
    super(METHOD);
    params = Arrays.asList(RawMessage.of(message));
  }
}
