package io.github.lbarnkow.jarb.bots.pugme;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditChildData {
  private String url;
  @JsonAlias("is_video")
  private boolean isVideo;
}
