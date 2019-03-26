package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawUser {
  private String _id;
  private String username;

  public User convert() {
    return User.builder() //
        .id(_id) //
        .name(username) //
        .build();
  }

  public static RawUser of(User u) {
    return RawUser.builder() //
        ._id(u.getId()) //
        .username(u.getName()) //
        .build();
  }
}
