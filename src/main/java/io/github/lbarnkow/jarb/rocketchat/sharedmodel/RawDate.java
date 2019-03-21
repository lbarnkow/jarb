package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.time.Instant;

import io.github.lbarnkow.jarb.misc.Common;

public class RawDate extends Common {
	private long $date;

	// Contents will be deserialized from JSON.
	RawDate() {
	}

	public RawDate(Instant date) {
		this.$date = date.getEpochSecond();
	}

	public long get$date() {
		return $date;
	}
}