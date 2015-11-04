package ipopprojekt.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a P2P message client
 */
public class P2PMessageClient {
	private final P2PMessageHandler messageHandler;
	private final P2PMessageReceived messageReceived;
	private final Socket socket;
	
	private final DataInputStream inputStream;
	private final DataOutputStream outputStream;
	
	/**
	 * Creates a new P2P message client that connects to the given message server
	 * @param messageHandler The message handler
	 * @param messageReceived Handles when a message is received
	 * @param address The address of the message server
	 * @param port The port of the message server
	 */
	public P2PMessageClient(P2PMessageHandler messageHandler, P2PMessageReceived messageReceived,
			InetAddress address, int port) throws IOException {
		this.socket = new Socket(address, port);
		this.messageHandler = messageHandler;
		this.messageReceived = messageReceived;
		
		this.inputStream = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
		this.outputStream = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
		
		//Start a listen thread
		Thread thread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (!socket.isClosed()) {
					try {
						P2PMessage msg = messageHandler.nextMessage(inputStream);
						
						if (msg != null) {
							synchronized (messageReceived) {
								messageReceived.received(msg);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						close();
					}
				}
			}
		});
		thread.start();
	}

	/**
	 * Sends a message to the server
	 * @param message The message to send
	 */
	public void sendMessage(P2PMessage message) {
		try {
			this.messageHandler.writeMessage(this.outputStream, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Indicates if the connection is closed
	 */
	public boolean isClosed() {
		return this.socket.isClosed();
	}
	
	/**
	 * Closes the connection to the server
	 */
	public void close() {
		try {
			this.socket.close();
			this.inputStream.close();
			this.outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
