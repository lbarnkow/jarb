package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.github.lbarnkow.jarb.misc.Common;

public class ReceiveLoginReply extends BaseMessage {

	private LoginResult result;

	// Contents will be deserialized from JSON.
	private ReceiveLoginReply() {
		super(null, null);
	}

	public LoginResult getResult() {
		return result;
	}

	public static class LoginResult extends Common {
		private String id;
		private String token;
		private TokenExpires tokenExpires;
		private String type;

		public String getId() {
			return id;
		}

		public String getToken() {
			return token;
		}

		public TokenExpires getTokenExpires() {
			return tokenExpires;
		}

		public String getType() {
			return type;
		}

		public static class TokenExpires extends Common {
			private long date;

			@JsonAlias("$date")
			public long getDate() {
				return date;
			}
		}
	}
}
