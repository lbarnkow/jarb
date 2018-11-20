package bot.rocketchat.util;

import bot.CommonBase;

public class ObjectHolder<T> extends CommonBase {
	private T value;

	ObjectHolder() {
	}

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
