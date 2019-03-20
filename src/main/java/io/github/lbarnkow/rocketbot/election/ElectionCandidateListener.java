package io.github.lbarnkow.rocketbot.election;

public interface ElectionCandidateListener {
	void onStateChanged(ElectionCandidate candidate, ElectionCandidateState oldState, ElectionCandidateState newState);
}
