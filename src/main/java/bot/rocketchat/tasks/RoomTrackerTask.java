package bot.rocketchat.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.rocketchat.rest.RestClient;

public class RoomTrackerTask implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RoomTrackerTask.class);

	public static final String ID_PREFIX = UUID.randomUUID().toString() + "-";

	private final RestClient rsClient;
	private final long sleepTimeMillis;

	private final RoomTrackerListener listener;

	public RoomTrackerTask(RestClient rsClient, long sleepTimeMillis, RoomTrackerListener listener) {
		this.rsClient = rsClient;
		this.sleepTimeMillis = sleepTimeMillis;
		this.listener = listener;
	}

	@Override
	public void run() {
		logger.debug("Unsubscribed public channel tracker refresh thread started.");

		while (!Thread.interrupted()) {
			List<String> channels = rsClient.getChannels();
			List<String> subs = rsClient.getSubscriptions();

			List<String> diff = new ArrayList<>(channels);
			diff.removeAll(subs);

			listener.onNewRooms(diff);

			try {
				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				break;
			}
		}

		logger.debug("Unsubscribed public channel tracker refresh thread stopped.");
	}

	public static interface RoomTrackerListener {
		void onNewRooms(List<String> newRooms);
	}
}