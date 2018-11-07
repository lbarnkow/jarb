package bot.rocketchat.websocket.messages;

public class RecChangedSub extends RecWithId {
	private String collection;

	public String getCollection() {
		return collection;
	}

	public static RecChangedSub parse(String json) {
		return parse(json, RecChangedSub.class);
	}
}
