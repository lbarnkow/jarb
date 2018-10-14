package messages.responses;

public class ResLogin extends ResMethodBase {
	private Result result;

	public Result getResult() {
		return result;
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
