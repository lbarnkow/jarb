package election;

import java.io.File;

public class Config {
	public static final String SYNC_FILE_NAME = "/tmp/si-rocket-chat-bot-lease.json";
	public static final File SYNC_FILE = new File(SYNC_FILE_NAME);

	public static final long LEASE_TIME_TO_LIVE_MSEC = 1000;
	public static final long LEASE_REFRESH_MSEC = 300;
	public static final long LEASE_CHALLENGE_MSEC = 100;

	public static final long SLEEP_VARIANCE_MSEC = (long) (Math.random() * 100);
}
