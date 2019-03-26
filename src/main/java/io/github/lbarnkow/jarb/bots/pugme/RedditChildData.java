package io.github.lbarnkow.jarb.bots.pugme;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditChildData {
  private String url;
  private boolean is_video;
}
