package ipopprojekt.client;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Represents a P2P receiver
 */
public class P2PReceiver {
	private final P2PMessageHandler messageHandler;
	private final MessageReceived messageReceived;
	private final ServerSocket socket;
	
	/**
	 * Creates a new P2P receiver at the given port
	 * @param messageHandler The message handler
	 * @param messageReceived Handles when a message is received
	 * @param port The port
	 */
	public P2PReceiver(P2PMessageHandler messageHandler, MessageReceived messageReceived, int port)
			throws IOException {
		this.messageHandler = messageHandler;
		this.messageReceived = messageReceived;
		this.socket = new ServerSocket(port);
		
		Thread acceptThread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (!socket.isClosed()) {
					
				}
			}
		});
		acceptThread.start();
	}
}
