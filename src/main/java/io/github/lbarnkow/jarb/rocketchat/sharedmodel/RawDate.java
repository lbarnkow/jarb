package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import lombok.Data;

@JarbJsonSettings
@Data
public class RawDate {
	@JsonAlias("$date")
	private long date;
}