package io.github.lbarnkow.jarb.bots.pugme;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditChildData {
  private String url;
  @JsonProperty("is_video")
  private boolean isVideo;
}
