package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RawSubscription {
  private String t;
  private RawDate ts;
  private RawDate ls;
  private String name;
  private String rid;
  private RawUser u;
  private boolean open;
  private boolean alert;
  // roles?
  private int unread;
  private RawDate _updatedAt;
  private String _id;
}
