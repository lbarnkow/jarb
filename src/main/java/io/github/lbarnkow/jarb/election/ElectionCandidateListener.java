package io.github.lbarnkow.jarb.election;

public interface ElectionCandidateListener {
  void onStateChanged(ElectionCandidate source, ElectionCandidateState oldState,
      ElectionCandidateState newState);
}
