package ipopprojekt.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

import ipopproject.messages.MessageId;

public class NetworkClient implements Runnable {
	private String name;
	private int chatRoom;
	
	private String serverName;
	private int serverPort;
	
	private Socket clientSocket;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	
	private int p2pPort = -1;
	private P2PClient p2pClient;
	private final ChatMessageReceived chatMessageReceived;
	
	/**
	 * Creates a new network client
	 * @param name The name of the client
	 * @param chatRoom The room
	 * @param chatMessageReceived Handles when a message is received
	 */
	public NetworkClient(String name, int chatRoom, ChatMessageReceived chatMessageReceived) {
		this.name = name;
		this.chatRoom = chatRoom;
		this.chatMessageReceived = chatMessageReceived;
		connect("localhost", 4711);
	}
	
	/**
	 * Returns the name of the client
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the chat room
	 */
	public int getChatRoom() {
		return chatRoom;
	}
	
	/**
	 * Returns the server name
	 */
	public String getServerName() {
		return this.serverName;
	}

	/**
	 * Returns the server port
	 */
	public int getServerPort() {
		return this.serverPort;
	}
	
	/**
	 * Indicates if the client is connected to the server
	 */
	public boolean isConnected() {
		if (this.clientSocket == null) {
			return false;
		}
		
		return !this.clientSocket.isClosed() && this.clientSocket.isConnected();
	}
	
	/**
	 * Opens the streams and socket
	 * @throws IOException 
	 */
	private void open() throws IOException {
		this.streamIn = new DataInputStream(new BufferedInputStream(this.clientSocket.getInputStream()));
		this.streamOut = new DataOutputStream(this.clientSocket.getOutputStream());
	}
	
	/**
	 * Closes the streams and socket
	 */
	private void close() {
		try {
			if (this.streamIn != null) {
				this.streamIn.close();
			}
			
			if (this.streamOut != null) {
				this.streamOut.close();
			}
			
			if (this.clientSocket != null) {
				this.clientSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Connects to the given server
	 * @param serverName The server name/IP
	 * @param serverPort The server port
	 * @return True if the client was connected else false
	 */
	public boolean connect(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
		
		System.out.println("Connecting to server...");
		
		try {
			this.clientSocket = new Socket(this.serverName, this.serverPort);				
			this.open();
			
			//Handle communication in a separate thread
			Thread clientThread = new Thread(this);
			clientThread.start();
						
			System.out.println("Connected to server: " + this.serverName + ":" + this.serverPort);	
			
			//Choose a random port
			Random random = new Random();
			this.p2pPort = 4712 + random.nextInt(10000);
			
			//Send it to the server
//			System.out.println("port = " + this.p2pPort);
			this.streamOut.writeByte(MessageId.CONNECT_CLIENT.getId());
			this.streamOut.writeInt(this.p2pPort);
			this.streamOut.writeInt(this.chatRoom);
			this.streamOut.flush();
			
			return true;
		} catch (UnknownHostException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("Could not connect to: " + this.serverName + ":" + this.serverPort);
		}
		
		return false;
	}
	
	/**
	 * Disconnects from the server
	 */
	public void disconnect() {
		System.out.println("Disconnected from server");
		this.close();
	}

	@Override
	public void run() {
		while (this.isConnected()) {
			//Handle commands
			try {
				//Read the message header
				byte messageID = streamIn.readByte();
				
				switch (MessageId.fromByte(messageID)) {
				case ADD_NEIGHBORS:
					{
						int num = streamIn.readInt();
						System.out.println("Adding neighbors: ");
						for (int i = 0; i < num; i++) {
							int userId = streamIn.readInt();
							InetSocketAddress userAddress = new InetSocketAddress(
								streamIn.readUTF(),
								streamIn.readInt());
							
							System.out.println(userId + ": " + userAddress);
							this.p2pClient.addNeighbor(userId, userAddress);
						}
					}
					break;
				case REMOVE_NEIGHBORS:
					{
						int num = streamIn.readInt();
						System.out.println("Removing neighbors: ");
						for (int i = 0; i < num; i++) {
							int userId = streamIn.readInt();
							System.out.println(userId);
							this.p2pClient.removeNeighbor(userId);
						}
					}
					break;
				case SET_USER_ID:
					{
						int userId = streamIn.readInt();
						System.out.println("userId = " + userId);
						this.p2pClient = new P2PClient(this.p2pPort, userId, this.name, chatMessageReceived);
					}
					break;
				default: break;
				}
			} catch (IOException e) {
				this.disconnect();
				break;
			}
		}
	}
	
	/**
	 * Sends the given message to the chat
	 * @param message The message
	 */
	public void sendMessage(String message) {
		this.p2pClient.send(message);
	}
}
