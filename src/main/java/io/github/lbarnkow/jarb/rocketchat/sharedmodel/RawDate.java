package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@MyJsonSettings
@Data
public class RawDate {
	@JsonAlias("$date")
	private long date;
}