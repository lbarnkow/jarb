package bot.rocketchat.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.CommonBase;
import bot.rocketchat.rest.RestClient;
import bot.rocketchat.rest.Room;
import bot.rocketchat.rest.Subscription;

public class RoomTrackerTask extends CommonBase implements Runnable {
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
			List<Room> channels = rsClient.getChannels();
			List<Subscription> subs = rsClient.getSubscriptions();

			List<Room> diff = new ArrayList<>(channels);
			diff.removeIf(room -> isRoomInSubscriptionList(room, subs));

			try {
				if (!diff.isEmpty())
					listener.onNewRooms(diff);
			} catch (Exception e) {
				logger.error("RoomTrackerListener threw an exception!", e);
			}

			try {
				Thread.sleep(sleepTimeMillis);
			} catch (InterruptedException e) {
				break;
			}
		}

		logger.debug("Unsubscribed public channel tracker refresh thread stopped.");
	}

	public static interface RoomTrackerListener {
		void onNewRooms(List<Room> newRooms);
	}

	private boolean isRoomInSubscriptionList(Room room, List<Subscription> subs) {
		for (Subscription sub : subs)
			if (sub.getRoomId().equals(room.getId()))
				return true;

		return false;
	}
}