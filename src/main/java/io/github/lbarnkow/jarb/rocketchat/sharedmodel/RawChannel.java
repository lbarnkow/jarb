package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import lombok.Data;

@MyJsonSettings
@Data
public class RawChannel {
	private String _id;
	private String name;
	private String t;
}