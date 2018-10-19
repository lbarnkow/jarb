package bot;

public interface MessageHandler {
	public Message handle(Message message);

	public static final class Message {
	}
}