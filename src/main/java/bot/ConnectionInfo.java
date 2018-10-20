package bot;

public class ConnectionInfo {
	private String serverUrl;
	private String username;
	// TODO: Don't store as password? Must be SHA-256 for login anyways.
	private String password;

	ConnectionInfo() {
		this(null, null, null);
	}

	public ConnectionInfo(String serverUrl, String username, String password) {
		this.serverUrl = serverUrl;
		this.username = username;
		this.password = password;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}