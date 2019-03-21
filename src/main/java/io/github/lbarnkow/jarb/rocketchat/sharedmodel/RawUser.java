package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.misc.Common;

public class RawUser extends Common {
	private String _id;
	private String username;

	public String get_id() {
		return _id;
	}

	public String getUsername() {
		return username;
	}
}