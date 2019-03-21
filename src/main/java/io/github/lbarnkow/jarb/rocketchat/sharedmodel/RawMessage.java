package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.util.Collections;
import java.util.List;

import lombok.Data;

@MyJsonSettings
@Data
public class RawMessage {
	private String _id;
	private String t;
	private String rid;
	private String msg;
	private String ts;
	private RawUser u;
	private List<RawAttachment> attachments = Collections.emptyList();
}