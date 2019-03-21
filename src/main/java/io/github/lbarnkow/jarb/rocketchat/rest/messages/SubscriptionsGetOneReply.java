package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionsGetOneReply extends BaseReply {
	private String rid;
	private String name;
	private String t;
}
