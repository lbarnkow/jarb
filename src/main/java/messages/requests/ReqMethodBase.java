package messages.requests;

import java.util.UUID;

public class ReqMethodBase extends ReqBase {
	private final String method;
	private final String id;

	public ReqMethodBase(String method) {
		super("method");
		this.method = method;
		this.id = UUID.randomUUID().toString();
	}

	public String getMethod() {
		return method;
	}

	public String getId() {
		return id;
	}
}
