package io.github.lbarnkow.rocketbot.election;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "syncFile" })
public class ElectionConfiguration {
	private static final long DEFAULT_LEASE_REFRESH_MSEC = 300L;
	private static final long DEFAULT_LEASE_CHALLENGE_MSEC = 100L;
	private static final long DEFAULT_LEASE_TIME_TO_LIVE_MSEC = 1000L;

	private long leaseRefreshInterval = DEFAULT_LEASE_REFRESH_MSEC;
	private long leaseChallengeInterval = DEFAULT_LEASE_CHALLENGE_MSEC;
	private long leaseTimeToLive = DEFAULT_LEASE_TIME_TO_LIVE_MSEC;
	String syncFileName = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + ".tmp")
			.getAbsolutePath();

	public long getLeaseRefreshInterval() {
		return leaseRefreshInterval;
	}

	public long getLeaseChallengeInterval() {
		return leaseChallengeInterval;
	}

	public long getLeaseTimeToLive() {
		return leaseTimeToLive;
	}

	public String getSyncFileName() {
		return syncFileName;
	}

	public File getSyncFile() {
		return new File(getSyncFileName());
	}
}
