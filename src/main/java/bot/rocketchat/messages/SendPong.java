package bot.rocketchat.messages;

public class SendPong extends Base {
	public SendPong() {
		super("pong");
	}

	public static SendPong parse(String json) {
		return parse(json, SendPong.class);
	}
}
