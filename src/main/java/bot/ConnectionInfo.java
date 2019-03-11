package bot;

import javax.inject.Singleton;

@Singleton
public class ConnectionInfo extends CommonBase {
	private boolean initialized = false;
	private boolean encrypted;
	private String hostname;
	private int port;
	private String username;
	private String password;

	public synchronized void initialize(boolean encrypted, String hostname, int port, String username,
			String password) {
		if (this.initialized)
			throw new IllegalStateException("ConnectionInfo already initialized!");

		this.encrypted = encrypted;
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.initialized = true;
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