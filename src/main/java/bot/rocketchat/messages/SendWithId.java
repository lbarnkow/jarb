package bot.rocketchat.messages;

import java.util.UUID;

public class SendWithId extends Base {
	private String id;

	protected SendWithId(String id, String msg) {
		super(msg);
		if (id == null)
			this.id = UUID.randomUUID().toString();
		else
			this.id = id;
	}

	protected SendWithId(String msg) {
		this(null, msg);
	}
}
