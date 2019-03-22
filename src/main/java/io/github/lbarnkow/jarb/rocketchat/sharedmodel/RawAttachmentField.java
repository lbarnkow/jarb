package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RawAttachmentField {
	@JsonAlias("short")
	private boolean bshort;
	private String title;
	private String value;
}