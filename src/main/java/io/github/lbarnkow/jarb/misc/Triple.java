package io.github.lbarnkow.jarb.misc;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Triple<A, B, C> extends Tuple<A, B> {
	private final C third;

	public Triple(A first, B second, C third) {
		super(first, second);
		this.third = third;
	}
}
