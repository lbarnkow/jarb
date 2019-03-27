package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RawSubscription {
  @JsonAlias("t")
  private String type;
  private RawDate ts;
  private RawDate ls;
  private String name;
  private String rid;
  @JsonAlias("u")
  private RawUser user;
  private boolean open;
  private boolean alert;
  // roles?
  private int unread;
  @JsonAlias("_updatedAt")
  private RawDate updatedAt;
  @JsonAlias("_id")
  private String id;
}
