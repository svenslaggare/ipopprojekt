package ipopprojekt.client;

/**
 * Indicates that a chat message has been received
 */
public interface ChatMessageReceived {
	/**
	 * Marks that the given message was received
	 * @param message The message
	 */
	void received(ChatMessage message);
}