package io.github.lbarnkow.jarb.bots.pugme;

import java.util.Collections;
import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RedditListingData {
	private List<RedditChild> children = Collections.emptyList();
}
