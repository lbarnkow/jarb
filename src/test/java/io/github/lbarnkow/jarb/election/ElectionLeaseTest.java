package io.github.lbarnkow.jarb.election;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.LINUX;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

class ElectionLeaseTest {

  private static final long DEFAULT_LEASE_TTL = 1000L;

  private String id = UUID.randomUUID().toString();

  @Test
  void testState() {
    // given

    // when
    ElectionLease lease = new ElectionLease(id, 5000L, 6000L);

    // then
    assertThat(lease.getLeaderId()).isEqualTo(id);
    assertThat(lease.getLeaseAcquired()).isEqualTo(5000L);
    assertThat(lease.getLeaseExpiration()).isEqualTo(6000L);
  }

  @Test
  void testIsOwnedBy() {
    // given
    String id2 = UUID.randomUUID().toString();
    ElectionLease lease1 = new ElectionLease(id, 0L);
    ElectionLease lease2 = new ElectionLease(id2, 0L);

    // when
    boolean idOwnsLease1 = lease1.isOwnedBy(id);
    boolean id2OwnsLease1 = lease1.isOwnedBy(id2);
    boolean idOwnsLease2 = lease2.isOwnedBy(id);
    boolean id2OwnsLease2 = lease2.isOwnedBy(id2);

    // then
    assertThat(idOwnsLease1).isTrue();
    assertThat(id2OwnsLease1).isFalse();
    assertThat(idOwnsLease2).isFalse();
    assertThat(id2OwnsLease2).isTrue();
  }

  @Test
  void testLeaseExpiration() throws InterruptedException {
    // given
    long now = System.currentTimeMillis();
    ElectionLease leaseA = new ElectionLease(id, now, now + 10L);

    // when
    boolean isExpiredBeforeSleep = leaseA.isExpired();
    Thread.sleep(15L);
    boolean isExpiredAfterSleep = leaseA.isExpired();

    // then
    assertThat(isExpiredBeforeSleep).isFalse();
    assertThat(isExpiredAfterSleep).isTrue();
  }

  @Test
  void testLeaseRenewal() {
    // given
    ElectionLease lease1 = new ElectionLease(id, 1000L, 2000L);

    // when
    ElectionLease lease2 = new ElectionLease(lease1, DEFAULT_LEASE_TTL);

    // then
    assertThat(lease1).isNotSameAs(lease2);
    assertThat(lease1.getLeaderId()).isEqualTo(lease2.getLeaderId());
    assertThat(lease1.isExpired()).isTrue();
    assertThat(lease2.isExpired()).isFalse();
  }

  @Test
  void testLoadAndSave() throws IOException {
    // given
    File tmpFile = Files.createTempFile("test", null).toFile();
    ElectionLease lease = new ElectionLease(id, DEFAULT_LEASE_TTL);

    // when
    ElectionLease.save(lease, tmpFile);
    ElectionLease lease2 = ElectionLease.load(tmpFile);

    // then
    assertThat(lease).isNotSameAs(lease2);
    assertThat(lease.getLeaderId()).isEqualTo(lease2.getLeaderId());
    assertThat(lease.getLeaseAcquired()).isEqualTo(lease2.getLeaseAcquired());
    assertThat(lease.getLeaseExpiration()).isEqualTo(lease2.getLeaseExpiration());
  }

  @Test
  void testLoadNonExistantFile() throws IOException {
    // given

    // when
    ElectionLease lease = ElectionLease.load(new File("/tmp/" + UUID.randomUUID().toString()));

    // then
    assertThat(lease).isNull();
  }

  @Test
  @EnabledOnOs({ LINUX })
  void testLoadWithBadPermissions() {
    // given

    // when
    assertThrows(IOException.class, () -> {
      ElectionLease.load(new File("/dev/rtc0"));
    });
  }

  @Test
  void testSaveWithBadTargetPath() {
    // given
    ElectionLease lease = new ElectionLease(id, DEFAULT_LEASE_TTL);

    // when
    assertThrows(IOException.class, () -> {
      ElectionLease.save(lease, new File("/dummy/folder/nonexisting"));
    });

    // then
  }

  @Test
  void testSaveWithExpiredLease() throws IOException {
    // given
    File tmpFile = Files.createTempFile("test", null).toFile();
    ElectionLease lease = new ElectionLease(id, 0L, 10L);

    // when
    assertThrows(ElectionLeaseExpiredException.class, () -> {
      ElectionLease.save(lease, tmpFile);
    });
  }
}
