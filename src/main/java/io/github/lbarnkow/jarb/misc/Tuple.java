package io.github.lbarnkow.jarb.misc;

public class Tuple<A, B> extends Common {
	private final A first;
	private final B second;

	public Tuple(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}
