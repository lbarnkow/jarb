package io.github.lbarnkow.rocketbot.tasks;

import java.util.HashSet;
import java.util.Set;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply.Subscription;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendGetSubscriptions;
import io.github.lbarnkow.rocketbot.taskmanager.Task;

public class SubscriptionsTrackerTask extends Task {

//	private static final Logger logger = LoggerFactory.getLogger(SubscriptionsTrackerTask.class);

	private static final long DEFAULT_SLEEP_TIME = 1000L * 5L; // 5 seconds

	private Bot bot;
	private RealtimeClient realtimeClient;
	private final long sleepTime;
	private final SubscriptionsTrackerTaskListener listener;

	private Set<String> knownIds = new HashSet<>();

	SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient, long sleepTime,
			SubscriptionsTrackerTaskListener listener) {
		this.bot = bot;
		this.realtimeClient = realtimeClient;
		this.sleepTime = sleepTime;
		this.listener = listener;
	}

	public SubscriptionsTrackerTask(Bot bot, RealtimeClient realtimeClient, SubscriptionsTrackerTaskListener listener) {
		this(bot, realtimeClient, DEFAULT_SLEEP_TIME, listener);
	}

	public Bot getBot() {
		return bot;
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		try {
			while (true) {
				SendGetSubscriptions message = new SendGetSubscriptions();
				ReceiveGetSubscriptionsReply reply = realtimeClient.sendMessageAndWait(message,
						ReceiveGetSubscriptionsReply.class);

				Set<String> newIds = new HashSet<>();

				for (Subscription sub : reply.getResult()) {
					newIds.add(sub.getId());

					if (!knownIds.contains(sub.getId())) {
						listener.onNewSubscription(this, sub.getRid());
					}
				}

				knownIds = newIds;

				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException e) {
		}
	}

	public static interface SubscriptionsTrackerTaskListener {
		void onNewSubscription(SubscriptionsTrackerTask subscriptionsTrackerTask, String roomId);
	}
}
