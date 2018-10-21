package bot.rocketchat.websocket.messages;

public class SendMethod extends SendWithId {
	private static final String MSG = "method";

	@SuppressWarnings("unused")
	private String method;

	protected SendMethod(String id, String method) {
		super(id, MSG);
		this.method = method;
	}

	protected SendMethod(String method) {
		this(null, method);
	}
}
