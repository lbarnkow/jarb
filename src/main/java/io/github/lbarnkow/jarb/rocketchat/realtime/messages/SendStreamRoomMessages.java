package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Room;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SendStreamRoomMessages extends BaseSubscription {
  private static final String NAME = "stream-room-messages";
  public static final String COLLECTION = NAME;

  private final Object[] params;

  public SendStreamRoomMessages(Room room) {
    super(NAME);
    params = new Object[] { room.getId(), false };
  }
}
