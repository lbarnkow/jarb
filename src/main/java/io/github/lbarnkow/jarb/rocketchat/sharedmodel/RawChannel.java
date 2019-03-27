package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JarbJsonSettings
@Data
@Builder
@NoArgsConstructor // Jackson needs this
@AllArgsConstructor // @Builder needs this
public class RawChannel {
  @JsonAlias("_id")
  private String id;
  private String name;
  @JsonAlias("t")
  private String type;

  public Room asRoom() {
    RoomType type = RoomType.parse(this.type);
    return Room.builder().id(id).name(name).type(type).build();
  }

  public static RawChannel of(Room room) {
    RawChannel channel = new RawChannel();
    channel.id = room.getId();
    channel.name = room.getName();
    channel.type = room.getType().getRawType();
    return channel;
  }
}
