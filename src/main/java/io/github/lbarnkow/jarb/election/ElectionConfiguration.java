package io.github.lbarnkow.jarb.election;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties({ "syncFile" })
@Data
public class ElectionConfiguration {
	private static final long DEFAULT_LEASE_REFRESH_MSEC = 300L;
	private static final long DEFAULT_LEASE_CHALLENGE_MSEC = 100L;
	private static final long DEFAULT_LEASE_TIME_TO_LIVE_MSEC = 1000L;

	private long leaseRefreshInterval = DEFAULT_LEASE_REFRESH_MSEC;
	private long leaseChallengeInterval = DEFAULT_LEASE_CHALLENGE_MSEC;
	private long leaseTimeToLive = DEFAULT_LEASE_TIME_TO_LIVE_MSEC;
	String syncFileName = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + ".tmp")
			.getAbsolutePath();

	public File getSyncFile() {
		return new File(getSyncFileName());
	}
}
