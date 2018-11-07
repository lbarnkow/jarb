package bot.rocketchat.util;

public enum RoomType {
	CHANNEL("c"), GROUP("p"), IM("d");

	private final String type;

	private RoomType(String type) {
		this.type = type;
	}

	public static RoomType parse(String type) {
		for (RoomType roomType : RoomType.values())
			if (roomType.type.equals(type))
				return roomType;

		throw new IllegalArgumentException("Cannot parse RoomType from String '" + type + "'!");
	}
}
