package bot.rocketchat.rest.responses;

import java.util.List;

public class GroupListResponse {
	private List<Group> groups;
	private int offset;
	private int count;
	private int total;
	private boolean success;

	public List<Group> getGroups() {
		return groups;
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

	public static class Group {
		private String _id;
		private boolean ro;

//        "name": "test-test",
//        "t": "p",
//        "msgs": 0,
//        "u": {
//            "_id": "aobEdbYhXfu5hkeqG",
//            "username": "testing1"
//        },
//        "ts": "2016-12-09T15:08:58.042Z",
//        "sysMes": true,
//        "_updatedAt": "2016-12-09T15:22:40.656Z"

		public String getId() {
			return _id;
		}

		public boolean isReadOnly() {
			return ro;
		}
	}
}
