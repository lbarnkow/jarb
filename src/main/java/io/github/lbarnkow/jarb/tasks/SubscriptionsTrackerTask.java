package io.github.lbarnkow.jarb.tasks;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendGetSubscriptions;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply.Subscription;

public class SubscriptionsTrackerTask extends AbstractBaseTask {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsTrackerTask.class);

	private static final long DEFAULT_SLEEP_TIME = 1000L * 15L; // 15 seconds

	private final Bot bot;
	private final RealtimeClient realtimeClient;
	private final long sleepTime;
	private final SubscriptionsTrackerTaskListener listener;

	SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient, long sleepTime,
			SubscriptionsTrackerTaskListener listener) {
		super(bot.getName());

		this.bot = bot;
		this.realtimeClient = realtimeClient;
		this.sleepTime = sleepTime;
		this.listener = listener;
	}

	public SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient, SubscriptionsTrackerTaskListener listener) {
		this(bot, realtimeClient, DEFAULT_SLEEP_TIME, listener);
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		Set<String> knownIds = new HashSet<>();
		try {
			while (true) {
				SendGetSubscriptions message = new SendGetSubscriptions();
				ReceiveGetSubscriptionsReply reply = realtimeClient.sendMessageAndWait(message,
						ReceiveGetSubscriptionsReply.class);

				Set<String> newIds = new HashSet<>();

				for (Subscription sub : reply.getResult()) {
					newIds.add(sub.getId());

					if (!knownIds.contains(sub.getId())) {
						Room room = new Room(sub.getRid(), sub.getName(), RoomType.parse(sub.getT()));
						logger.debug("Bot '{}' has a subscription to room '{}'.", bot.getName(), room.getName());
						listener.onNewSubscription(this, bot, room);
					}
				}

				knownIds = newIds;
				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException e) {
		}
	}

	public static interface SubscriptionsTrackerTaskListener {
		void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room);
	}
}
