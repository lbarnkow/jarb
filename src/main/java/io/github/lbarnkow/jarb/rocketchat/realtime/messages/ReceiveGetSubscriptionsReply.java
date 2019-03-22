package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.List;

import io.github.lbarnkow.jarb.JarbJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawSubscription;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JarbJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveGetSubscriptionsReply extends BaseMessage {
	private List<RawSubscription> result;
}
