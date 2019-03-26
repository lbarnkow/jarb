package io.github.lbarnkow.jarb.bots.pugme;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditListingData {
  private List<RedditChild> children = Collections.emptyList();
}
