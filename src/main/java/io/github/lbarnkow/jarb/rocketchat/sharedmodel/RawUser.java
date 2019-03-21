package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import lombok.Data;

@MyJsonSettings
@Data
public class RawUser {
	private String _id;
	private String username;
}