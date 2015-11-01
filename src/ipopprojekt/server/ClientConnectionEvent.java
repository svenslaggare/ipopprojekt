package ipopprojekt.server;

import java.io.IOException;

/**
 * The ClientConnectionEvent interface handles connection events
 */
public interface ClientConnectionEvent
{
	/**
	 * Fires when an client is connected to the server
	 * @param client The client
	 */
	public void clientConnected(Client client) throws IOException;
	
	/**
	 * Fires when an client is connected to the server
	 * @param client The client
	 */
	public void clientDisconnected(Client client);		
}