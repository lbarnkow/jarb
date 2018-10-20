package bot.rocketchat.messages;

public class RecWithId extends Base {
	private String id;

	public String getId() {
		return id;
	}

	public static RecWithId parse(String json) {
		return parse(json, RecWithId.class);
	}
}
