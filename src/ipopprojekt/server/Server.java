package ipopprojekt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The central server that handles all connections
 * and delegate to whom each client shall send messages.
 */
public class Server implements Runnable {
	private int port;
	private ServerSocket serverSocket;
	
	private List<Client> clients;
	private int maxClientCount;
	
	private ClientConnectionEvent clientConnectionEvent;
	
	private boolean isRunning = false;
	
	public Server(int port, int maxClientCount) {
		this.port = port;
		this.maxClientCount = maxClientCount;
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
				//When we close the server, the serverSocket.accept() throws an exception, so ignore this exception if the server isn't running
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
				this.serverSocket = new ServerSocket(this.port);
				
				this.clients = new ArrayList<Client>();
				
				Thread clientConnectionThread = new Thread(this);
				clientConnectionThread.start();
				
				this.isRunning = true;
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
	
	/**
	 * Stops the server
	 */
	public synchronized void stop() {
		if (this.isRunning)	{
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
	public synchronized void addClient(Socket clientSocket) {
		if (this.clients.size() < this.maxClientCount) {
			Client newClient = new Client(clientSocket, this);
			
			try	{
				//Open the IO streams
				newClient.open();
				
				//Create the client thread
				Thread clientThread = new Thread(newClient);
				clientThread.start();
				
				//Add the client
				this.clients.add(newClient);
				
				if (this.clientConnectionEvent != null) {
					this.clientConnectionEvent.clientConnected(newClient);
				}
			} catch(IOException e) {
				System.err.println("Error opening client: " + e);
			}	
		}
	}
	
	/**
	 * Removes the given client from the list of clients
	 * @param client The client
	 * @return True if the client was removed else false
	 */
	public synchronized boolean removeClient(Client client) {
		if (client == null) {
			return false;
		}
		
		synchronized (this.clients) {
			if (this.clients.contains(client)) {
				if (this.clientConnectionEvent != null) {				
					this.clientConnectionEvent.clientDisconnected(client);
				}
				
				this.clients.remove(client);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Returns clients connected to the server
	 */
	public synchronized List<Client> getClients() {
		return this.clients;
	}
	
	/**
	 * Returns the requested client
	 * @param color The color of the player object
	 * @return The client or null if the client wasn't found
	 */
	public synchronized Client getClient(int id) {
		if (this.clients.size() < id) {
			return this.clients.get(id);
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server(4711, 30);
		
		server.start();
	}
}