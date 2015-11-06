package ipopprojekt.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a P2P client that handles message exchange
 */
public class P2PClient implements P2PMessageReceived {
	private final P2PMessageHandler messageHandler;
	private final ChatMessageReceived chatMessageReceived;
	private final DatagramSocket clientSocket;
	private boolean connected = true;
	
	public final int MAX_PACKET_SIZE = 10 * 1024;
	
	private final Map<Integer, InetSocketAddress> neighbors = new HashMap<>();
	
	/**
	 * Creates a new P2P client
	 * @param port The port used
	 * @param userId The id of the client
	 * @param name The name of the user
	 * @param chatMessageReceived Handles when a chat message is received for the client
	 */
	public P2PClient(int port, int userId, String name, ChatMessageReceived chatMessageReceived) throws SocketException {
		this.messageHandler = new P2PMessageHandler(userId, name);
		this.clientSocket = new DatagramSocket(port);
		this.chatMessageReceived = chatMessageReceived;
		
		//Listen for incoming messages
		Thread receiveThread = new Thread(new Runnable() {		
			@Override
			public void run() {		
				byte[] buffer = new byte[MAX_PACKET_SIZE];
				
				while (connected) {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					
					try {
						clientSocket.receive(packet);
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
			}
		});
		receiveThread.start();
	}
	
	/**
	 * Adds a neighbor to current client
	 * @param userId The id of the user
	 * @param address The socket address of the user
	 */
	public void addNeighbor(int userId, InetSocketAddress address) {
		synchronized (this.neighbors) {
			this.neighbors.put(userId, address);
		}
	}
	
	/**
	 * Removes the given user as neighbor
	 * @param userId The id of the user
	 */
	public void removeNeighbor(int userId) {
		synchronized (this.neighbors) {
			this.neighbors.remove(userId);
		}
	}
	
	/**
	 * Sends the given message
	 * @param message The message
	 */
	public void send(String message) {
		this.sendMessage(this.messageHandler.createMessage(message));
	}
	
	/**
	 * Sends the given message to all neighbors
	 * @param message The message
	 */
	private void sendMessage(P2PMessage message) {
		try {
			//Create a binary version of the message
			byte[] data = null;
			try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				this.messageHandler.writeMessage(new DataOutputStream(stream), message);
				data = stream.toByteArray();
			}
	
			//Send to each neighbor
			synchronized (this.neighbors) {
				for (InetSocketAddress neighbor : this.neighbors.values()) {
					DatagramPacket packet = new DatagramPacket(data, data.length, neighbor);
					this.clientSocket.send(packet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Marks that the given message was received
	 * @param message The message
	 */
	@Override
	public void received(P2PMessage message) {
		//To avoid displaying own messages
		if (message.getSenderId() == this.messageHandler.getUserId()) {
			return;
		}
		
		//Display the message
		synchronized (this.chatMessageReceived) {
			this.chatMessageReceived.received(new ChatMessage(
				LocalDateTime.now(),
				message.getSenderName(),
				message.getMessage()));
		}
		
		//Relay it to other clients
		this.sendMessage(message);
	}
	
	/**
	 * Closes the client
	 */
	public void close() {
		this.connected = false;
	}
}
