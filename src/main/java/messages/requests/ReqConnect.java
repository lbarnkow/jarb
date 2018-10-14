package messages.requests;

public class ReqConnect extends ReqBase {
	private String version = "1";
	private String[] support = new String[] { "1" };

	public ReqConnect() {
		super("connect");
	}

	public String getVersion() {
		return version;
	}

	public String[] getSupport() {
		return support;
	}
}
