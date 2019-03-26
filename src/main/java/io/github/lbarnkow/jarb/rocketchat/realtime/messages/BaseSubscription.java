package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseSubscription extends BaseMessage {
  private static final String MSG = "sub";

  private final String name;

  BaseSubscription(String name) {
    super(MSG, UUID.randomUUID().toString());
    this.name = name;
  }
}
