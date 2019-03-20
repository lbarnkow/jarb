package io.github.lbarnkow.jarb.misc;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class CommonTest {

	@Test
	void testToString() throws IllegalArgumentException, IllegalAccessException {
		// given
		Pojo pojo = new Pojo();

		// when
		String string = pojo.toString();

		// then
		for (Field field : pojo.getClass().getDeclaredFields()) {
			if (field.isSynthetic()) {
				continue; // jacoco dynamically instruments classes
			}

			boolean accessible = field.isAccessible();
			field.setAccessible(true);

			String name = field.getName();
			String value = field.get(pojo).toString();

			assertThat(string).contains(name);
			assertThat(string).contains(value);

			field.setAccessible(accessible);
		}
	}

	private static final class Pojo extends Common {
		@SuppressWarnings("unused")
		private int number = 42;
		@SuppressWarnings("unused")
		private String text = "testing";
	}
}
