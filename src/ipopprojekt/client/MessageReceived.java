package ipopprojekt.client;

/**
 * Indicates that a message has been received
 */
public interface MessageReceived {
	/**
	 * Marks that the given message was received
	 * @param message The message
	 */
	void received(P2PMessage message);
}
