package bot.rocketchat;

public interface RocketChatClientListener {
	Message onRocketChatClientMessage(Message message);
	
	void onRocketChatClientClose(boolean initiatedByClient);
}