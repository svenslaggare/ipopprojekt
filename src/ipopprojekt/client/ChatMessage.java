package ipopprojekt.client;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a chat message
 */
public class ChatMessage {
	private final LocalDateTime sent;
	private final String sender;
	private final String message;
	
	/**
	 * Creates a new chat message
	 * @param sent The time which the message was sent
	 * @param sender The name of the sender
	 * @param message The message
	 */
	public ChatMessage(LocalDateTime sent, String sender, String message) {
		this.sent = sent;
		this.sender = sender;
		this.message = message;
	}

	/**
	 * Returns the time which the message was sent
	 */
	public LocalDateTime getSent() {
		return sent;
	}

	/**
	 * Returns the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Returns the message
	 */
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return String.format(
			"[%s] %s: %s",
			this.sent.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
			this.sender,
			this.message);
	}
}
