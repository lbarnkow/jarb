package io.github.lbarnkow.jarb.api;

public enum RoomType {
  PUBLIC_CHANNEL("c"), //
  PRIVATE_GROUP("g"), //
  INSTANT_MESSAGE("d"), //
  LIVE_CHAT("l"); //

  private final String rawType;

  private RoomType(String rawType) {
    this.rawType = rawType;
  }

  public String getRawType() {
    return rawType;
  }

  public static RoomType parse(String rawType) {
    for (RoomType type : RoomType.values()) {
      if (type.rawType.equals(rawType)) {
        return type;
      }
    }

    throw new IllegalArgumentException(
        "Parameter rawType '" + rawType + "' doesn't map to any known RoomType instance!");
  }
}
