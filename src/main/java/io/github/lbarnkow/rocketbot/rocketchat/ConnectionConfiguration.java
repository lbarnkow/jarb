package io.github.lbarnkow.rocketbot.rocketchat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "websocketUrl", "restUrl" })
public class ConnectionConfiguration {
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8080;
	private static final boolean DEFAULT_IS_ENCRYPTED = false;

	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	private boolean encrypted = DEFAULT_IS_ENCRYPTED;

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public String getWebsocketUrl() {
		String scheme = encrypted ? "wss" : "ws";
		return scheme + "://" + host + ":" + port + "/websocket/";
	}

	public String getRestUrl() {
		String scheme = encrypted ? "https" : "http";
		return scheme + "://" + host + ":" + port + "/api/v1";
	}
}
