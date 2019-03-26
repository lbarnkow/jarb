package io.github.lbarnkow.jarb.bots;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
import lombok.Getter;
import lombok.ToString;

@ToString
public abstract class AbstractBaseBot implements Bot {
  @Getter
  private String name;
  @Getter
  private Credentials credentials;

  @Override
  public AbstractBaseBot initialize(String name, Credentials credentials) {
    this.name = name;
    this.credentials = credentials;

    return this;
  }
}
