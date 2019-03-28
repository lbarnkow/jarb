package io.github.lbarnkow.jarb.election;

import static io.github.lbarnkow.jarb.election.ElectionCandidateState.INACTIVE;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.LEADER;
import static io.github.lbarnkow.jarb.election.ElectionCandidateState.RUNNING_FOR_ELECTION;

import io.github.lbarnkow.jarb.taskmanager.AbstractBaseTask;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElectionCandidate extends AbstractBaseTask {

  static final long SLEEP_VARIANCE_MSEC = (long) (Math.random() * 100L);

  private final String id = UUID.randomUUID().toString();

  volatile ElectionCandidateState state = null;
  private ElectionCandidateListener listener;
  private ElectionConfiguration config;

  /**
   * Post-construction initialization by the user of this instance. Since this
   * instance will most likely be created by the dependency injection framework,
   * this method is used to parameterize it further.
   *
   * @param listener the listener to inform about state changes
   * @param config   the parsed configuration
   * @return
   */
  public ElectionCandidate configure(ElectionCandidateListener listener,
      ElectionConfiguration config) {
    this.listener = listener;
    this.config = config;
    return this;
  }

  public boolean isLeader() {
    return state == LEADER;
  }

  public String getId() {
    return id;
  }

  @Override
  public void runTask() throws Throwable {
    updateState(INACTIVE);

    try {
      while (true) {
        try {
          ElectionLease leaseFile = readLeaseFile();

          handleMissingLeaseFile(leaseFile);
          handleStolenLeaseFile(leaseFile);
          handleExpiredLeaseFile(leaseFile);

          if (state == LEADER || state == RUNNING_FOR_ELECTION) {
            leaseFile = refreshLease(leaseFile);
          } else {
            challengeLease(leaseFile);
          }
        } catch (IOException e) {
          log.error("IOException occurred in LeaseManager!", e);
        }

        sleep();
      }
    } catch (InterruptedException e) {
      log.info("Caught InterruptedException, withdrawing from election!");
    }

    try {
      releaseLease();
    } catch (IOException e) {
      log.error("Unexpected IOException in LeaseManager!", e);
    }
  }

  private ElectionLease readLeaseFile() throws IOException {
    ElectionLease lease = ElectionLease.load(config.getSyncFile());

    if (lease == null) {
      updateState(INACTIVE);
    }

    return lease;
  }

  void writeLeaseFile(ElectionLease lease) throws IOException {
    try {
      ElectionLease.save(lease, config.getSyncFile());
    } catch (IOException e) {
      updateState(INACTIVE);
      throw e;
    }
  }

  private void releaseLease() throws IOException {
    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      Files.delete(config.getSyncFile().toPath());
      log.info("Released lease (deleted lease file)!");
    }
    updateState(INACTIVE);
  }

  private void challengeLease(ElectionLease leaseFile) throws IOException {
    if (leaseFile == null || leaseFile.isExpired()) {
      ElectionLease newLease = new ElectionLease(id, config.getLeaseTimeToLive());
      writeLeaseFile(newLease);

      log.info("Running for election w/ id '{}'", id);

      updateState(RUNNING_FOR_ELECTION);
    }
  }

  private ElectionLease refreshLease(ElectionLease oldLease) throws IOException {
    ElectionLease newLease = new ElectionLease(oldLease, config.getLeaseTimeToLive());
    writeLeaseFile(newLease);

    if (state == RUNNING_FOR_ELECTION) {
      log.info("Won election w/ id '{}'! Promoted to state '{}'!", id, LEADER);
    }
    updateState(LEADER);

    return newLease;
  }

  private void updateState(ElectionCandidateState newState) {
    if (state == newState) {
      return;
    }

    ElectionCandidateState oldState = state;
    state = newState;
    log.trace("Switched to state '{}'.", state.toString());

    listener.onStateChanged(this, oldState, newState);
  }

  private void sleep() throws InterruptedException {
    long sleepTime = SLEEP_VARIANCE_MSEC;

    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      sleepTime += config.getLeaseRefreshInterval();
    } else {
      sleepTime += config.getLeaseChallengeInterval();
    }

    Thread.sleep(sleepTime);
  }

  void handleMissingLeaseFile(ElectionLease leaseFile) {
    if (leaseFile != null) {
      return;
    }

    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      log.error("Lease file missing even though my state was '{}'! Reverting back to '{}'.", state,
          INACTIVE);
    }

    updateState(INACTIVE);
  }

  void handleStolenLeaseFile(ElectionLease leaseFile) {
    if (leaseFile == null) {
      return;
    }

    if (state != LEADER && state != RUNNING_FOR_ELECTION) {
      return;
    }
    if (leaseFile.isOwnedBy(id)) {
      return;
    }

    if (state == LEADER) {
      log.error("Lease file stolen by '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(),
          INACTIVE);
    } else {
      log.info("Lost election to '{}'! Reverting back to '{}'.", leaseFile.getLeaderId(), INACTIVE);
    }

    updateState(INACTIVE);
  }

  void handleExpiredLeaseFile(ElectionLease leaseFile) {
    if (leaseFile == null) {
      return;
    }

    if (!leaseFile.isExpired()) {
      return;
    }

    if (state == LEADER || state == RUNNING_FOR_ELECTION) {
      log.error("Lease file expired during my term (state '{}')! Reverting back to '{}'.", state,
          INACTIVE);
    }

    updateState(INACTIVE);
  }
}
