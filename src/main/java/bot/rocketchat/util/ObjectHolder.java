package bot.rocketchat.util;

public class ObjectHolder<T> {
	private T value;

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void reset() {
		set(null);
	}

	public boolean isEmpty() {
		return value == null;
	}
}
