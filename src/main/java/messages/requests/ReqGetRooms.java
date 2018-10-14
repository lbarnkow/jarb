package messages.requests;

public class ReqGetRooms extends ReqMethodBase {
	private final Params[] params;

	public ReqGetRooms(long $date) {
		super("rooms/get");
		this.params = new Params[] { new Params($date) };
	}

	public Params[] getParams() {
		return params;
	}

	public static class Params {
		private final long $date;

		public Params(long $date) {
			this.$date = $date;
		}

		public long get$date() {
			return $date;
		}
	}
}
