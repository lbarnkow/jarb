package io.github.lbarnkow.rocketbot.misc;

public class Holder<T> extends Common {
	private final T initialValue;
	private T value;

	public Holder(T value) {
		this.initialValue = value;
		this.value = value;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		if (value == null) {
			throw new IllegalArgumentException("Holder can't reference null values!");
		}
		this.value = value;
	}

	public void reset() {
		set(initialValue);
	}
}
