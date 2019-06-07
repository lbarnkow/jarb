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
  /**
   * The default time interval between renewing the election lease to extend the
   * leadership term.
   */
  private static final long LEASE_REFRESH_MS = 300L;

  /**
   * The default time to wait before trying to run for election.
   */
  private static final long LEASE_RETRY_MS = 100L;

  /**
   * The default time-to-live for an (possibly orphaned) election lease ticket,
   * before a candidate can try to run for election.
   */
  private static final long LEASE_TTL_MS = 1000L;

  /**
   * The default file name for file containing the election lease for
   * synchronisation.
   */
  private static final String SYNC_FILE_NAME =
      new File(System.getProperty("java.io.tmpdir"), "jarb-election-sync-file.tmp")
          .getAbsolutePath();

  /**
   * The time interval between renewing the election lease to extend the
   * leadership term.
   */
  private long leaseRefreshMs = LEASE_REFRESH_MS;

  /**
   * The time to wait before trying to run for election.
   */
  private long leaseRetryMs = LEASE_RETRY_MS;

  /**
   * The time-to-live for an (possibly orphaned) election lease ticket, before a
   * candidate can try to run for election.
   */
  private long leaseTtl = LEASE_TTL_MS;

  /**
   * The file name for file containing the election lease for synchronisation.
   */
  private String syncFileName = SYNC_FILE_NAME;

  /**
   * Gets a <code>File</code> instance representing the election synchronisation
   * file.
   *
   * @return the election synchronisation <code>File</code>
   */
  public File getSyncFile() {
    return new File(getSyncFileName());
  }
}
