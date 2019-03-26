package io.github.lbarnkow.jarb.bots.pugme;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditResponse {
  private RedditListingData data;
}
