package ipopprojekt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ipopproject.messages.MessageID;

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
				this.clients = new ArrayList<Client>();
				
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
	public synchronized void stop() {
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
				
				System.out.println("Client accepted: " + clientSocket.getRemoteSocketAddress());
				
				sendSendList(newClient);
				
				if (this.clientConnectionEvent != null) {
					this.clientConnectionEvent.clientConnected(newClient);
				}
			} catch(IOException e) {
				System.err.println("Error opening client: " + e);
			}	
		} else {
			System.out.println("Maximum clients reached (" + this.maxClientCount + ")");
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
				System.out.println("Client: '" + client.toString() + "' removed");
				
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
	
	private void sendSendList(Client client) {
		try {
			client.getOutputStream().writeByte(MessageID.SEND_LIST.getId());
			
			List<Client> sendTo = getSendList(client);
			
			if (sendTo.size() > 0) {
				client.getOutputStream().writeShort(sendTo.size());
				
				for (Client receiver : sendTo) {
					client.getOutputStream().writeUTF(receiver.getIP());
					client.getOutputStream().writeInt(receiver.getPort());
				}
				
				client.getOutputStream().flush();
			}
		} catch (IOException e) {
			System.err.println("Could not send sender list: " + e);
		}
	}
	
	private List<Client> getSendList(Client client) {
		List<Client> sendList = new ArrayList<Client>();
		
		for (Client receiver : clients) {
			if (receiver.getID() != client.getID()) {
				sendList.add(receiver);
			}
		}
		
		return sendList;
	}
	
	public static void main(String[] args) {
		Server server = new Server(4711, 30);
		
		server.start();
	}
}