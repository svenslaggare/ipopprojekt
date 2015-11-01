package ipopprojekt.client;

/**
 * Indicates that a P2P message has been received
 */
public interface P2PMessageReceived {
	/**
	 * Marks that the given message was received
	 * @param message The message
	 */
	void received(P2PMessage message);
}
