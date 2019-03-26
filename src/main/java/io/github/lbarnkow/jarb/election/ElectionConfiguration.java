package io.github.lbarnkow.jarb.election;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.io.File;
import lombok.Data;

//@JsonIgnoreProperties({ "syncFile" })
@JarbJsonSettings
@Data
public class ElectionConfiguration {
  private static final long DEFAULT_LEASE_REFRESH_MSEC = 300L;
  private static final long DEFAULT_LEASE_CHALLENGE_MSEC = 100L;
  private static final long DEFAULT_LEASE_TIME_TO_LIVE_MSEC = 1000L;
  private static final String DEFAULT_SYNC_FILE_NAME =
      new File(System.getProperty("java.io.tmpdir"), "jarb-election-sync-file.tmp")
          .getAbsolutePath();

  private long leaseRefreshInterval = DEFAULT_LEASE_REFRESH_MSEC;
  private long leaseChallengeInterval = DEFAULT_LEASE_CHALLENGE_MSEC;
  private long leaseTimeToLive = DEFAULT_LEASE_TIME_TO_LIVE_MSEC;
  private String syncFileName = DEFAULT_SYNC_FILE_NAME;

  public File getSyncFile() {
    return new File(getSyncFileName());
  }
}
