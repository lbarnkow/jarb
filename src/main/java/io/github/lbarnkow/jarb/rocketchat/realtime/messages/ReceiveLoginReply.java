package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import io.github.lbarnkow.jarb.misc.Common;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawDate;

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
		private RawDate tokenExpires;
		private String type;

		public String getId() {
			return id;
		}

		public String getToken() {
			return token;
		}

		public RawDate getTokenExpires() {
			return tokenExpires;
		}

		public String getType() {
			return type;
		}
	}
}
