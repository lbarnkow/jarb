package bot.rocketchat.websocket.messages;

public class RecLogin extends RecWithId {
	private Result result;

	public Result getResult() {
		return result;
	}

	public static RecLogin parse(String json) {
		return parse(json, RecLogin.class);
	}

	public static class Result {
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
	}

	public static class TokenExpires {
		private String $date;

		public String get$date() {
			return $date;
		}
	}
}
