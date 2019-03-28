package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RawSubscription {
  @JsonProperty("t")
  private String type;
  private RawDate ts;
  private RawDate ls;
  private String name;
  private String rid;
  @JsonProperty("u")
  private RawUser user;
  private boolean open;
  private boolean alert;
  // roles?
  private int unread;
  @JsonProperty("_updatedAt")
  private RawDate updatedAt;
  @JsonProperty("_id")
  private String id;
}
