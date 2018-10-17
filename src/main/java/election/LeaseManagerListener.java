package election;

public interface LeaseManagerListener {
	void onStateChanged(String id, State newState);
}
