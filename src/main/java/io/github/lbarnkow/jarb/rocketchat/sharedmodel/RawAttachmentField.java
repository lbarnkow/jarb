package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@MyJsonSettings
@Data
public class RawAttachmentField {
	@JsonAlias("short")
	private boolean bshort;
	private String title;
	private String value;
}