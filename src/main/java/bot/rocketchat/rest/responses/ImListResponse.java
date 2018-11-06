package bot.rocketchat.rest.responses;

import java.util.List;

public class ImListResponse {
	private List<Im> ims;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	public List<Im> getIms() {
		return ims;
	}

	public int getOffset() {
		return offset;
	}

	public int getCount() {
		return count;
	}

	public int getTotal() {
		return total;
	}

	public boolean isSuccessful() {
		return success;
	}

	public static class Im {
		private String _id;
//		private String _updatedAt;
//		private String t;
//		private int msgs;
//		private String ts;
//		private String lm;
//		private String topic;

		public String getId() {
			return _id;
		}
	}
}
