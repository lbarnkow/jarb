/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb.election;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.io.File;
import lombok.Data;

/**
 * POJO representation of 'election' subsection of the main configuration file.
 *
 * @author lbarnkow
 */
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
