package election.demo;

import static election.State.LEADER;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import election.LeaseManager;
import election.LeaseManagerListener;
import election.State;

public class LeaseManagerDemoMain {
	private static final Logger logger = LoggerFactory.getLogger(LeaseManagerDemoMain.class);

	public static void main(String... args) throws InterruptedException {
		LeaseManagerListener listener = new LeaseManagerListener() {
			@Override
			public void onStateChanged(String id, State newState) {
				if (newState == LEADER)
					logger.info("{} changed state to '{}'", id, newState);
			}
		};

		List<LeaseManager> lm = new ArrayList<>();
		for (int i = 0; i < 30; i++)
			lm.add(new LeaseManager(listener));

		for (LeaseManager item : lm)
			spawn(item);

		for (int i = 0; i < 50; i++) {
			logger.error("*********************************************************");
			logger.error("Stopping LEADER w/ id '{}'!", getLeader(lm).getId());
			logger.error("*********************************************************");
			restart(getLeader(lm));
		}

		for (LeaseManager item : lm)
			item.stop();
	}

	private static LeaseManager getLeader(List<LeaseManager> lm) {
		for (LeaseManager item : lm)
			if (item.isLeader())
				return item;
		return lm.get(0);
	}

	private static void spawn(LeaseManager lm) throws InterruptedException {
		Thread t = new Thread(lm);
//		t.setDaemon(true);
		t.start();
		Thread.sleep(150);
	}

	private static void restart(LeaseManager lm) throws InterruptedException {
		lm.stop();
		Thread.sleep(1000);
		spawn(lm);
	}
}
