package io.github.lbarnkow.jarb.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
	String id;
	String name;
}
