package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Room;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendJoinRoom extends BaseMessageWithMethod {
  private static final String METHOD = "joinRoom";

  private final List<String> params = new ArrayList<>();

  public SendJoinRoom(String roomId, String joinCode) {
    super(METHOD);

    this.params.add(roomId);
    if (joinCode != null) {
      this.params.add(joinCode);
    }
  }

  public SendJoinRoom(Room room) {
    this(room.getId(), null);
  }
}
