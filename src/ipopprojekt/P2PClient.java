package ipopprojekt;

/**
 * Represents a P2P client
 */
public class P2PClient {
	private final P2PMessageHandler messageHandler;
	
	/**
	 * Creates a new P2P client
	 * @param userId The id of the client
	 */
	public P2PClient(int userId) {
		this.messageHandler = new P2PMessageHandler(userId);
	}
}
