package bot;

public class ThreadProvider {
	ThreadProvider() {
	}

	public Thread create(Runnable runnable, String name) {
		return new Thread(runnable, name);
	}
}
