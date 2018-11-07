package bot.rocketchat.websocket.messages;

public abstract class SendSub extends SendWithId {
	private static final String MSG = "sub";

	@SuppressWarnings("unused")
	private String name;

	public SendSub(String name) {
		super(MSG);
		this.name = name;
	}
}
