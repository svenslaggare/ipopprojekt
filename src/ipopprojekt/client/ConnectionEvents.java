package ipopprojekt.client;

/**
 * The connection events
 */
public interface ConnectionEvents {
	/**
	 * The client connected to the server
	 */
	void connected();
	
	/**
	 * The client could not connect
	 */
	void failedToConnect();
	
	/**
	 * The client was disconnected from the server
	 */
	void disconnected();
}
