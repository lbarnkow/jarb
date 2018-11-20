package bot.rocketchat.websocket.messages.out;

public abstract class SendSub extends SendWithId {
	private static final String MSG = "sub";

	@SuppressWarnings("unused")
	private String name;

	SendSub() {
	}

	public SendSub initialize(String name) {
		super.initialize(MSG);
		this.name = name;
		return this;
	}
}
