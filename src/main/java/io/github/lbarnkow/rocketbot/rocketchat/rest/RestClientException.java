package io.github.lbarnkow.rocketbot.rocketchat.rest;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class RestClientException extends Exception {
	private static final long serialVersionUID = 245562960868828052L;

	public RestClientException(Object object) {
		super("Bad response from REST endpoint: " + ToStringBuilder.reflectionToString(object, SHORT_PREFIX_STYLE));
	}
}
