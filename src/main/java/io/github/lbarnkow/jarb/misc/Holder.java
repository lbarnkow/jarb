package io.github.lbarnkow.jarb.misc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Holder<T> {
	private final T initialValue;
	@NonNull
	@Getter
	@Setter
	private T value;

	public Holder(T value) {
		this.initialValue = value;
		this.value = value;
	}

	public void reset() {
		setValue(initialValue);
	}
}
