package bot.rocketchat.messages;

public class SendConnect extends Base {
	private String version = "1";
	private String[] support = new String[] { "1" };

	public SendConnect() {
		super("connect");
	}

	public String getVersion() {
		return version;
	}

	public String[] getSupport() {
		return support;
	}

	public static SendConnect parse(String json) {
		return parse(json, SendConnect.class);
	}
}
