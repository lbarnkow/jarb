package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelListReply extends BaseReply {
	private List<RawChannel> channels;
}
