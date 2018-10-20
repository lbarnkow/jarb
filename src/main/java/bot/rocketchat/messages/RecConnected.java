package bot.rocketchat.messages;

public class RecConnected extends Base {
	private String session;

	public String getSession() {
		return session;
	}

	public static RecConnected parse(String json) {
		return parse(json, RecConnected.class);
	}
}
