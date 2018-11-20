package bot.rocketchat.websocket.messages.in;

public class RecChangedSub extends RecWithId {
	private String collection;

	RecChangedSub() {
	}

	public String getCollection() {
		return collection;
	}
}
