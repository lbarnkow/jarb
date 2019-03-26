package io.github.lbarnkow.jarb.api;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthInfo {
  public static final AuthInfo INVALID = new AuthInfo(null, null, Instant.ofEpochMilli(0));

  private final String userId;
  private final String authToken;
  private final Instant expires;

  public boolean isValid() {
    return expires.isAfter(Instant.now());
  }
}