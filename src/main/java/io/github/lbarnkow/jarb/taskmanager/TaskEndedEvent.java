package io.github.lbarnkow.jarb.taskmanager;

import java.util.Optional;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskEndedEvent {
  private Task task;
  private TaskState state;
  private Optional<Throwable> lastError;
}
