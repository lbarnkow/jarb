package io.github.lbarnkow.jarb.api;

import lombok.Getter;

public enum MessageType {
	REGULAR_CHAT_MESSAGE(null), //

	ROOM_NAME_CHANGED("r"), //
	USER_ADDED_BY("au"), //
	USER_REMOVED_BY("ru"), //
	USER_JOINED_ROOM("uj"), //
	USER_LEFT_ROOM("ul"), //
	WELCOME("wm"), //
	MESSAGE_REMOVED("rm"), //
	MESSAGE_PINNED("message_pinned"), //
	MESSAGE_SNIPPETED("message_snippeted"), //
	USER_MUTED_BY("user-muted"), //
	USER_UNMUTED_BY("user-unmuted"), //
	SUBSCRIPTION_ROLE_ADDED_BY("subscription-role-added"), //
	SUBSCRIPTION_ROLE_REMOVED_BY("subscription-role-removed"), //
	ROOM_ARCHIVED("room-archived"), //
	ROOM_UNARCHIVED("room-unarchived"), //
	ROOM_CHANGED_PRIVACY("room_changed_privacy"), //
	ROOM_CHANGED_TOPIC("room_changed_topic"), //
	ROOM_CHANGED_ANNOUNCEMENT("room_changed_announcement"), //
	ROOM_CHANGED_DESCRIPTION("room_changed_description"), //

	THREAD_CREATED("thread-created"), //
	THREAD_WELCOME("thread-welcome"), //
	USER_JOINED_THREAD("ut"), //

	JITSI_CALL_STARTED("jitsi_call_started"), //
	RTC("rtc"), //
	REJECTED_MESSAGE_BY_PEER("rejected-message-by-peer"), //
	PEER_DOES_NOT_EXIST("peer-does-not-exist"), //

	LIVECHAT_NAVIGATION_HISTORY("livechat_navigation_history"), //
	LIVECHAT_VIDEO_CALL("livechat_video_call"), //
	LIVECHAT_CLOSE("livechat-close"), //

	UNKNOWN(null);

	@Getter
	private final String rawType;

	private MessageType(String rawType) {
		this.rawType = rawType;
	}

	public static MessageType parse(String rawType) {
		if (rawType == null) {
			return REGULAR_CHAT_MESSAGE;
		}

		for (MessageType type : MessageType.values()) {
			if (type.rawType == null) {
				continue;
			}
			if (type.rawType.equals(rawType)) {
				return type;
			}
		}

		return UNKNOWN;
	}
}
