package io.github.lbarnkow.jarb.api;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.bots.DummyBot;
import lombok.Data;

@JarbJsonSettings
@Data
public class BotConfiguration {
	private static final String DEFAULT_NAME = "demobot";
	private static final String DEFAULT_QUALIFIED_CLASS_NAME = DummyBot.class.getName();
	private static final Credentials DEFAULT_CREDENTIALS = new Credentials("demobot", DigestUtils.sha256Hex("demobot"));

	private String name = DEFAULT_NAME;
	private String qualifiedClassName = DEFAULT_QUALIFIED_CLASS_NAME;
	private Credentials credentials = DEFAULT_CREDENTIALS;
	private Map<String, Object> settings = Collections.emptyMap();
}
