package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawMessage;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryReply extends BaseReply {
  private List<RawMessage> messages;
}
