package election;

public interface LeaseManagerListener {
	void onStateChanged(State newState);
}
