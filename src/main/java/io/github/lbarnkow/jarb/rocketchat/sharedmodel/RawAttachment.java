package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import io.github.lbarnkow.jarb.misc.Common;

public class RawAttachment extends Common {
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}