package io.github.lbarnkow.rocketbot.rocketchat.realtime.messages;

//public class SendLoadHistory extends BaseMessageWithMethod {
//	private static final String METHOD = "loadHistory";
//
//	@SuppressWarnings("unused")
//	private final List<Object> params;
//
//	public SendLoadHistory(String roomId, Instant excludeNewerThan, int count, Instant firstUnread) {
//		super(METHOD);
//
//		Date upperLimit = null;
//		if (excludeNewerThan != null) {
//			upperLimit = new Date(excludeNewerThan);
//		}
//		Date lowerLimit = null;
//		if (firstUnread != null) {
//			lowerLimit = new Date(firstUnread);
//		}
//
//		this.params = Arrays.asList(roomId, upperLimit, count, lowerLimit);
//	}
//}
