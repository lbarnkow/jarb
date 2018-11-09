package bot;

public class ConnectionInfo extends CommonBase {
	private boolean encrypted;
	private String hostname;
	private int port;
	private String username;
	// TODO: Don't store password as String? Must be SHA-256 for login anyways.
	private String password;

	public ConnectionInfo(boolean encrypted, String hostname, int port, String username, String password) {
		this.encrypted = encrypted;
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getWebsocketUrl() {
		String scheme = encrypted ? "wss" : "ws";
		return scheme + "://" + hostname + ":" + port + "/websocket/";
	}

	public String getRestUrl() {
		String scheme = encrypted ? "https" : "http";
		return scheme + "://" + hostname + ":" + port + "/api/v1";
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}