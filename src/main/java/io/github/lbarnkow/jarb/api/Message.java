package io.github.lbarnkow.jarb.api;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Message {
  String id;
  Room room;
  String message;
  Instant timestamp;
  User user;

  @Builder.Default
  List<Attachment> attachments = Collections.emptyList();

  MessageType type;
}
