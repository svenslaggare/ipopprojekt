package ipopprojekt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ipopproject.messages.MessageId;
import ipopprojekt.server.ChatNetwork.Change;
import ipopprojekt.server.ChatNetwork.Changes;

/**
 * The central server that handles all connections
 * and delegate to whom each client shall send messages.
 */
public class Server implements Runnable {
	private final int port;
	private ServerSocket serverSocket;
	
	private List<Client> clients;
	private int nextID = 0;
	
	private ClientConnectionEvent clientConnectionEvent;
	
	private boolean isRunning = false;
	private ChatNetwork chatNetwork;
	
	/**
	 * Creates a new server that listens on the given port
	 * @param port The port
	 */
	public Server(int port) {
		this.port = port;
	}

	/**
	 * Indicates if the server is running
	 */
	public synchronized boolean isRunning() {
		return this.isRunning;
	}
	
	/**
	 * The ClientConnectionEvent handles connection events
	 * @param clientConnectionEvent The client connection event interface
	 */
	public void setClientConnectionEvent(ClientConnectionEvent clientConnectionEvent) {
		this.clientConnectionEvent = clientConnectionEvent;
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
				this.clients = new ArrayList<Client>();
				this.chatNetwork = new ChatNetwork(1);
				
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
				
				synchronized (this.clients) {
					//Close the connection to all sockets
					for (Client client : this.clients) {
						client.close();
					}
				}
			} catch (IOException e) {
				
			} finally {
				this.clients.clear();
			}
		}
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
			
			//Add the client
			synchronized (this.clients) {
				this.clients.add(newClient);			
				System.out.println("Client accepted: " + clientSocket.getRemoteSocketAddress());
			}
			
			if (this.clientConnectionEvent != null) {
				this.clientConnectionEvent.clientConnected(newClient);
			}
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
		
		synchronized (this.clients) {
			if (this.clients.contains(client)) {
				System.out.println("Client: '" + client.toString() + "' removed");
				
				if (this.clientConnectionEvent != null) {				
					this.clientConnectionEvent.clientDisconnected(client);
				}
				
				this.clientDisconnected(client);
				
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Returns clients connected to the server
	 */
	public List<Client> getClients() {
		return this.clients;
	}
	
	/**
	 * Returns the client with the given id
	 * @param id The id of the client
	 * @return The client or null
	 */
	public Client getClient(int id) {
		for (Client client : this.clients) {
			if (client.getId() == id) {
				return client;
			}
		}
		
		return null;
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
	 * Handles that the given client has connected
	 * @param newClient The newly connected client
	 */
	public void clientConnected(Client newClient) {	
//		//Send first to the newly connected client
//		this.sendAddNeighbors(newClient, this.getAllClients(newClient));
//		
//		//The to the others
//		for (Client client : this.clients) {
//			if (client != newClient) {
//				this.sendAddNeighbors(client, Collections.singletonList(newClient));
//			}
//		}
		
		//Add the client and distribute the changes
		for (Changes changes : this.chatNetwork.addClient(newClient.getId())) {
			List<Client> toAdd = new ArrayList<>();
			List<Client> toRemove = new ArrayList<>();
			
			for (Change change : changes.getChanges()) {
				switch (change.getType()) {
				case ADD:
					toAdd.add(this.getClient(change.getClientId()));
					break;
				case REMOVE:
					toRemove.add(this.getClient(change.getClientId()));
					break;
				default:
					break;
				}
			}
			
			this.sendAddNeighbors(this.getClient(changes.getClientId()), toAdd);
			this.sendRemoveNeighbors(this.getClient(changes.getClientId()), toRemove);
		}
	}
	
	/**
	 * Handles that the given client has disconnected
	 * @param client The client
	 */
	public void clientDisconnected(Client client) {
		this.clients.remove(client);
		
//		//Send to other clients that the client disconnected
//		for (Client other : this.clients) {
//			this.sendRemoveNeighbors(other, Collections.singletonList(client));
//		}
		
		//Remove the client and distribute the changes
		for (Changes changes : this.chatNetwork.removeClient(client.getId())) {
			List<Client> toAdd = new ArrayList<>();
			List<Client> toRemove = new ArrayList<>();
			
			for (Change change : changes.getChanges()) {
				switch (change.getType()) {
				case ADD:
					toAdd.add(this.getClient(change.getClientId()));
					break;
				case REMOVE:
					toRemove.add(this.getClient(change.getClientId()));
					break;
				default:
					break;
				}
			}
			
			this.sendAddNeighbors(this.getClient(changes.getClientId()), toAdd);
			this.sendRemoveNeighbors(this.getClient(changes.getClientId()), toRemove);
		}
	}
	
	/**
	 * Sends what neighbors to add for the given client
	 * @param client The client
	 * @param toAdd The clients to add
	 */
	private void sendAddNeighbors(Client client, List<Client> toAdd) {
		try {
			if (toAdd.size() > 0) {
				client.getOutputStream().writeByte(MessageId.ADD_NEIGHBORS.getId());		
				this.sendNeighborList(client, toAdd);				
				client.getOutputStream().flush();
			}
		} catch (IOException e) {
			System.err.println("Could not send addNeighbor " + e);
		}
	}
	
	/**
	 * Sends what neighbors to remove for the given client
	 * @param client The client
	 * @param toRemove The clients to remove
	 */
	private void sendRemoveNeighbors(Client client, List<Client> toRemove) {
		try {
			if (toRemove.size() > 0) {
				client.getOutputStream().writeByte(MessageId.REMOVE_NEIGHBORS.getId());		
				
				client.getOutputStream().writeInt(toRemove.size());
				for (Client receiver : toRemove) {
					client.getOutputStream().writeInt(receiver.getId());
				}
				
				client.getOutputStream().flush();
			}
		} catch (IOException e) {
			System.err.println("Could not send addNeighbor " + e);
		}
	}
	
	/**
	 * Sends the given neighbor list to the given client.
	 * Note that this method does not set the type of the message.
	 * @param client The client
	 * @param neighborList The neighbor list
	 */
	private void sendNeighborList(Client client, List<Client> neighborList) {
		try {
			client.getOutputStream().writeInt(neighborList.size());
			
			for (Client receiver : neighborList) {
				client.getOutputStream().writeInt(receiver.getId());
				client.getOutputStream().writeUTF(receiver.getIP());
				client.getOutputStream().writeInt(receiver.getPort());
			}
		} catch (IOException e) {
			System.err.println("Could not send neighbor list: " + e);
		}
	}
	
	/**
	 * Returns all clients except the given
	 * @param client The client
	 */
	private List<Client> getAllClients(Client client) {
		List<Client> clients = new ArrayList<Client>();
		
		for (Client receiver : this.clients) {
			if (receiver.getId() != client.getId()) {
				clients.add(receiver);
			}
		}
		
		return clients;
	}
	
	public static void main(String[] args) {
		Server server = new Server(4711);
		server.start();
	}
}