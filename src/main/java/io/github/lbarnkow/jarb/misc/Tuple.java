package io.github.lbarnkow.jarb.misc;

import lombok.Data;

@Data
public class Tuple<A, B> {
  private final A first;
  private final B second;
}
