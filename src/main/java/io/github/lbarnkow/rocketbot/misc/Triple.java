package io.github.lbarnkow.rocketbot.misc;

public class Triple<A, B, C> extends Tuple<A, B> {
	private final C third;

	public Triple(A first, B second, C third) {
		super(first, second);
		this.third = third;
	}

	public C getThird() {
		return third;
	}
}
