package io.github.lbarnkow.jarb.rocketchat.rest.messages;

import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelListReply extends BaseReply {
	private List<RawChannel> channels;
}
