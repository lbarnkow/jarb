package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.misc.Common;

public class RawChannel extends Common {
	private String _id;
	private String name;
	private String t;

	public String get_id() {
		return _id;
	}

	public String getName() {
		return name;
	}

	public String getT() {
		return t;
	}
}