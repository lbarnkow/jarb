package bot.rocketchat.websocket.messages.in;

public class RecLogin extends RecWithId {
	private Result result;

	RecLogin() {
	}

	public Result getResult() {
		return result;
	}

	public static class Result {
		private String id;
		private String token;
		private TokenExpires tokenExpires;
		private String type;

		Result() {
		}

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
	}

	public static class TokenExpires {
		private String $date;

		TokenExpires() {
		}

		public String get$date() {
			return $date;
		}
	}
}
