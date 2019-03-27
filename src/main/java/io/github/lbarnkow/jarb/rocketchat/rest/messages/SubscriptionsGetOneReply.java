package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionsGetOneReply extends BaseReply {
  private String rid;
  private String name;
  @JsonAlias("t")
  private String type;
}
