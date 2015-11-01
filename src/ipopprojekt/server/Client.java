package ipopprojekt.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client implements Runnable {
	private Socket socket;
	private Server server;
	private int id = -1;
	private String name;
	
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	
	/**
	 * Creates an new client
	 * @param socket The socket for the client
	 * @param server The server that the client is associated with
	 */
	public Client(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
		this.id = this.socket.getPort();
	}
	
	public int getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the player's name if it hasen't already been set
	 * @param name The name
	 * @return True if the object was set else false
	 */
	public synchronized boolean setName(String name) {
		if (this.name == null) {
			this.name = name;
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the client is connected
	 */
	public boolean isConnected() {
		return this.socket.isConnected() && !this.socket.isClosed();
	}
	
	@Override
	public void run() {
		while (this.isConnected()) {
			//Handle commands
			try {
				//Read the message header
				int commandSize = this.streamIn.readInt();
				short numberOfCommands = this.streamIn.readShort();
				
				//Read the command buffer
				byte[] commandData = new byte[commandSize];				
				this.streamIn.read(commandData, 0, commandSize);
				ByteBuffer commandBuffer = ByteBuffer.wrap(commandData);
			} catch (IOException e) {
				this.server.removeClient(this);
				break;
			}
		}
	}
	
	/**
	 * Opens the IO streams for the client
	 * @throws IOException If an IO exception happens
	 */
	public void open() throws IOException {
		this.streamIn = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
		this.streamOut = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
	}
	
	/**
	 * Closes the connection to the client
	 * @throws IOException If an IO exception happens
	 */
	public void close() throws IOException {
		if (this.socket != null) {
			this.socket.close();
		}
		
		if (this.streamOut != null) {
			this.streamOut.close();
		}
		
		if (this.streamOut != null) {
			this.streamOut.close();
		}
	}
}
