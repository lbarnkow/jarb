package bot;

public class ConnectionInfo {
	private String serverUrl;
	private String username;
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