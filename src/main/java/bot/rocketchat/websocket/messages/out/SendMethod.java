package bot.rocketchat.websocket.messages.out;

public class SendMethod extends SendWithId {
	private static final String MSG = "method";

	@SuppressWarnings("unused")
	private String method;

	SendMethod() {
	}

	private SendMethod _initialize(String id, String method) {
		super.initialize(id, MSG);
		this.method = method;
		return this;
	}

	public SendMethod initialize(String id, String method) {
		return _initialize(id, method);
	}

	public SendMethod initialize(String method) {
		return _initialize(null, method);
	}
}
