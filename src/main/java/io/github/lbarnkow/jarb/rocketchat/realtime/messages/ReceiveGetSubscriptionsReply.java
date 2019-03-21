package io.github.lbarnkow.jarb.rocketchat.realtime.messages;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.github.lbarnkow.jarb.rocketchat.sharedmodel.MyJsonSettings;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawDate;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MyJsonSettings
@Data
@EqualsAndHashCode(callSuper = true)
public class ReceiveGetSubscriptionsReply extends BaseMessage {
	private List<Subscription> result;

	@MyJsonSettings
	@Data
	public static class Subscription {
		@JsonAlias("_id")
		private String id; // "_id": "subscription-id"
		private String t; // "t": "d"
		private RawDate ts; // "ts": { "$date": 1480377601 }
		private RawDate ls; // "ls": { "$date": 1480377601 }
		private String name; // "name": "username"
		private String rid; // "rid": "room-id"
		private RawUser u; // "u": { "_id": "user-id", "username": "username" }
		private boolean open; // "open": true
		private boolean alert; // "alert": false
		private int unread; // "unread": 0
		private RawDate updatedAt; // "_updatedAt": { "$date": 1480377601 }
	}
}
