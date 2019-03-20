package io.github.lbarnkow.jarb.election;

public interface ElectionCandidateListener {
	void onStateChanged(ElectionCandidate candidate, ElectionCandidateState oldState, ElectionCandidateState newState);
}
