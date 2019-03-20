package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.shared;

import java.time.Instant;

import io.github.lbarnkow.rocketbot.misc.Common;

public class Date extends Common {
	private long $date;

	// Contents will be deserialized from JSON.
	Date() {
	}

	public Date(Instant date) {
		this.$date = date.getEpochSecond();
	}

	public long get$date() {
		return $date;
	}
}