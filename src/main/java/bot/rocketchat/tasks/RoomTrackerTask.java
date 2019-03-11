package bot.rocketchat.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.CommonBase;
import bot.rocketchat.rest.RestClient;
import bot.rocketchat.rest.entities.Room;
import bot.rocketchat.rest.entities.Subscription;

public class RoomTrackerTask extends CommonBase implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(RoomTrackerTask.class);

	public static final String ID_PREFIX = UUID.randomUUID().toString() + "-";

	private boolean initialized;
	private volatile boolean alive;
	private Thread myThread;

	private RestClient rsClient;
	private long sleepTimeMillis;
	private RoomTrackerListener listener;

	RoomTrackerTask() {
	}

	public void initialize(RestClient rsClient, long sleepTimeMillis, RoomTrackerListener listener) {
		if (this.initialized)
			throw new IllegalStateException("RoomTrackerTask already initialized!");

		this.rsClient = rsClient;
		this.sleepTimeMillis = sleepTimeMillis;
		this.listener = listener;
		this.initialized = true;
	}

	@Override
	public void run() {
		logger.debug("Unsubscribed public channel tracker refresh thread started.");
		myThread = Thread.currentThread();
		alive = true;

		while (alive) {
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

	public void stop() {
		alive = false;
		myThread.interrupt();

		while (myThread.isAlive()) {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
			}
		}
	}

	private boolean isRoomInSubscriptionList(Room room, List<Subscription> subs) {
		for (Subscription sub : subs)
			if (sub.getRoomId().equals(room.getId()))
				return true;

		return false;
	}
}