package messages.requests;

public abstract class ReqBase {
	private final String msg;

	public ReqBase(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}
