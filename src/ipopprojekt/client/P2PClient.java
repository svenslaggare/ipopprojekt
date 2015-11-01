package ipopprojekt.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Represents a P2P client
 */
public class P2PClient implements MessageReceived {
	private final P2PMessageHandler messageHandler;
	private final DatagramSocket receiverSocket;
	private boolean connected = true;
	
	public final int CLIENT_PORT = 4711;
	public final int MAX_PACKET_SIZE = 10 * 1024;
	
	/**
	 * Creates a new P2P client
	 * @param userId The id of the client
	 */
	public P2PClient(int userId) throws SocketException {
		this.messageHandler = new P2PMessageHandler(userId);
		this.receiverSocket = new DatagramSocket(CLIENT_PORT);
		
		Thread receiveThread = new Thread(new Runnable() {		
			@Override
			public void run() {
				byte[] buffer = new byte[MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				try {
					receiverSocket.receive(packet);
					try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(buffer))) {
						P2PMessage msg = messageHandler.nextMessage(stream);
						
						if (msg != null) {
							synchronized (messageHandler) {
								received(msg);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		receiveThread.start();
	}

	/**
	 * Marks that the given message was received
	 * @param message The message
	 */
	@Override
	public void received(P2PMessage message) {
		
	}
}
