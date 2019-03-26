package io.github.lbarnkow.jarb.bots.pugme;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditChild {
  private RedditChildData data;
}
