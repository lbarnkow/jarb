package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionsGetOneReply extends BaseReply {
  private String rid;
  private String name;
  private String t;
}
