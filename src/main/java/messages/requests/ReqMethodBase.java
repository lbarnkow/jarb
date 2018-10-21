package messages.requests;

import java.util.UUID;

public class ReqMethodBase extends ReqBase {
	private final String method;
	private final String id;

	public ReqMethodBase(String id, String method) {
		super("method");
		this.id = id;
		this.method = method;
	}

	public ReqMethodBase(String method) {
		this(UUID.randomUUID().toString(), method);
	}

	public String getMethod() {
		return method;
	}

	public String getId() {
		return id;
	}
}
