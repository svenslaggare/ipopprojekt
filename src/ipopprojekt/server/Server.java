package ipopprojekt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ipopproject.messages.MessageId;

/**
 * The central server that handles all connections
 * and delegate to whom each client shall send messages.
 */
public class Server implements Runnable {
	private final int port;
	private ServerSocket serverSocket;
	
	private int nextID = 0;
	
	private boolean isRunning = false;
	
	private final List<ChatRoom> chatRooms;
	
	/**
	 * Creates a new server that listens on the given port
	 * @param port The port
	 */
	public Server(int port) {
		this.port = port;
		this.chatRooms = new ArrayList<>();
	}

	/**
	 * Indicates if the server is running
	 */
	public synchronized boolean isRunning() {
		return this.isRunning;
	}
	
	@Override
	public void run() {
		while (this.isRunning) {
			try	{
				// Waits for an client to connect
				Socket clientSocket = this.serverSocket.accept();
				this.addClient(clientSocket);
			} catch (IOException e) {
				//When we close the server, the serverSocket.accept() throws an exception, 
				// so ignore this exception if the server isn't running
				if (this.isRunning) {
					System.err.println("Server accept error: " + e);
					this.stop();
				}
			}
		}
	}
	
	/**
	 * Starts the server
	 */
	public void start() {
		if (!this.isRunning) {
			try	{
				System.out.println("Starting server at port: " + port + "...");
				
				this.serverSocket = new ServerSocket(this.port);
				
				System.out.println("Server started: " + this.serverSocket.getLocalSocketAddress());
				System.out.println("Waiting for clients...");
				
				Thread clientConnectionThread = new Thread(this);
				clientConnectionThread.start();
				
				this.isRunning = true;
			} catch (IOException e) {
				System.err.println("Server start error: " + e);
			}
		}
	}
	
	/**
	 * Stops the server
	 */
	public void stop() {
		if (this.isRunning)	{
			System.out.println("Server stopped");
			
			this.isRunning = false;
			
			try {
				this.serverSocket.close();
				
				synchronized (this.chatRooms) {
					//Close the connection to all sockets
					for (ChatRoom room : chatRooms) {
						room.close();
					}
				}
			} catch (IOException e) {
				
			} finally {
				chatRooms.clear();
			}
		}
	}
	
	/**
	 * Adds a new chat room.
	 */
	public void addRoom() {
		chatRooms.add(new ChatRoom(chatRooms.size()));
	}
	
	/**
	 * Adds an new client
	 * @param clientSocket The socket for the client
	 */
	public void addClient(Socket clientSocket) {
		Client newClient = new Client(clientSocket, this, nextID++);
		
		try	{
			//Open the IO streams
			newClient.open();
			
			//Create the client thread
			Thread clientThread = new Thread(newClient);
			clientThread.start();
			
			//Send the id to the client
			this.sendClientId(newClient);
			
			//Send the rooms to the client
			this.sendRooms(newClient);
			
			System.out.println("Client accepted: " + clientSocket.getRemoteSocketAddress());
		} catch(IOException e) {
			System.err.println("Error opening client: " + e);
		}	
	}
	
	/**
	 * Removes the given client from the list of clients
	 * @param client The client
	 * @return True if the client was removed else false
	 */
	public boolean removeClient(Client client) {
		if (client == null) {
			return false;
		}
		
		synchronized (this.chatRooms) {
			ChatRoom room = findRoom(client);
			
			if (room != null) {
				return room.removeClient(client);
			}
		}
		
		return false;
	}
	
	/**
	 * Sends the client id to the given client
	 * @param client The client
	 */
	private void sendClientId(Client client) {
		try {
			client.getOutputStream().writeByte(MessageId.SET_USER_ID.getId());
			client.getOutputStream().writeInt(client.getId());
			client.getOutputStream().flush();
		} catch (IOException e) {
			System.err.println("Could not send client id: " + e);
		}
	}
	
	/**
	 * Sends the rooms to the client
	 * @param client The client
	 */
	private void sendRooms(Client client) {
		try {
			client.getOutputStream().writeByte(MessageId.SET_NUMBER_OF_ROOMS.getId());
			client.getOutputStream().writeInt(chatRooms.size());
			client.getOutputStream().flush();
		} catch (IOException e) {
			System.err.println("Could not send rooms: " + e);
		}
	}
	
	/**
	 * Handles that the given client has connected
	 * @param client The newly connected client
	 * @param chatRoom The chat room to join.
	 */
	public void clientConnected(Client client, int chatRoom) {
		synchronized (chatRooms) {
			if (chatRoom > 0 && chatRoom <= chatRooms.size()) {
				ChatRoom room = chatRooms.get(chatRoom - 1);
				room.addClient(client);
			} else {
				System.err.println("Invalid room: " + chatRoom);
				
				client.close();
			}
		}
	}
	
	/**
	 * Finds what room a client is in.
	 * 
	 * @param client The client
	 * @return The room the client is in.
	 */
	private ChatRoom findRoom(Client client) {
		for (ChatRoom room : chatRooms) {
			if (room.inRoom(client)) {
				return room;
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		int port = 4711;
		
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Invalid port");
				return;
			}
		}
		
		Server server = new Server(port);
		
		server.addRoom();
		server.addRoom();
		server.addRoom();
		
		server.start();
	}
}