package io.github.lbarnkow.rocketbot.misc;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Common {

	private static final Logger logger = LoggerFactory.getLogger(RealtimeClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	@Override
	public String toString() {
		try {
			return MAPPER.writeValueAsString(this);
		} catch (Exception e) {
			String name = getClass().getSimpleName();
			logger.error("Failed to serialize '{}' to JSON!", name, e);
			return name;
		}
	}

	public String tooString() {
		return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
	}
}
