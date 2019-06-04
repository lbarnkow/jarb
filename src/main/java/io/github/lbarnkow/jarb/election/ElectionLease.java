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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.lbarnkow.jarb.JarbJsonSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a leadership lease file as POJO.
 *
 * @author lbarnkow
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JarbJsonSettings
@Slf4j
public class ElectionLease {
  /**
   * Maximum number of retries upon IOExceptions.
   */
  private static final int MAX_FILE_IO_RETRIES = 5;

  /**
   * The <code>ObjectMapper</code> to de-/serialize the election lease file.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

  /**
   * The <code>ElectionCandidate</code> id claiming leadership in this lease.
   */
  private String leaderId;

  /**
   * The timestamp when leadership was acquired or refreshed.
   */
  private long leaseAcquired;

  /**
   * The timestamp when leadership will expire unless it is refreshed beforehand.
   */
  private long leaseExpiration;

  private ElectionLease(final String leaderId, final long ttl) {
    this(leaderId, System.currentTimeMillis(), System.currentTimeMillis() + ttl);
  }

  /**
   * Constructing a lease from a <code>ElectionCandidate</code> starting from now
   * and expiring after a given time-to-live.
   *
   * @param leader the candidate
   * @param ttl    the time-to-live for this lease
   */
  public ElectionLease(final ElectionCandidate leader, final long ttl) {
    this(leader.getId(), ttl);
  }

  /**
   * Constructing a lease reusing the id from a previous lease.
   *
   * @param oldLease the previous lease
   * @param ttl      the time-to-live for this lease
   */
  public ElectionLease(final ElectionLease oldLease, final long ttl) {
    this(oldLease.leaderId, ttl);
  }

  /**
   * Gets whether or not this lease has expired.
   *
   * @return <code>true</code> if this lease has expired; <code>false</code>
   *         otherwise
   */
  public boolean isExpired() {
    final long now = System.currentTimeMillis();
    return leaseExpiration < now;
  }

  /**
   * Gets whether or not this lease is owned by a given
   * <code>ElectionCandidate</code>.
   *
   * @param candidate the candidate
   * @return <code>true</code> if the id of the given candidate matches the leader
   *         id in this lease; <code>false</code> otherwise
   */
  public boolean isOwnedBy(final ElectionCandidate candidate) {
    return Objects.equals(leaderId, candidate.getId());
  }

  /**
   * Deserializes and loads <code>ElectionLease</code> data from the file system.
   *
   * @param file the file to load from
   * @return the deserialized <code>ElectionLease</code> instance
   * @throws IOException on io errors
   */
  public static ElectionLease load(final File file) throws IOException {
    if (!file.exists()) {
      return null;
    }

    ElectionLease lease = null;
    IOException lastException = null;
    int tries = 0;

    while (lease == null && tries < MAX_FILE_IO_RETRIES) {
      tries++;
      try {
        lease = MAPPER.readValue(file, ElectionLease.class);
      } catch (final IOException e) {
        lastException = e;
      }
    }

    if (lease == null) {
      if (lastException instanceof JsonProcessingException) {
        log.warn("Failed to deserialize election lease file; assuming file was empty!");
      } else {
        throw lastException;
      }
    }

    return lease;
  }

  /**
   * Serializes and stores <code>ElectionLease</code> data to the file system.
   *
   * @param lease the <code>ElectionLease</code> instance to serialize
   * @param file  the file to save to
   * @throws IOException on io errors
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static void save(final ElectionLease lease, final File file) throws IOException {
    boolean success = false;
    IOException lastException = null;
    final int tries = 0;

    final Path tmp = Files.createTempFile(UUID.randomUUID().toString(), null);
    MAPPER.writeValue(tmp.toFile(), lease);

    while (!lease.isExpired() && !success && tries < MAX_FILE_IO_RETRIES) {
      try {
        Files.move(tmp, file.toPath(), REPLACE_EXISTING);
        success = true;
      } catch (final IOException e) {
        lastException = e;
      }
    }

    if (tmp.toFile().exists()) {
      tmp.toFile().delete();
    }

    if (lastException == null && lease.isExpired()) {
      throw new ElectionLeaseExpiredException();
    } else if (!success) {
      throw lastException;
    }
  }
}