package ipopprojekt.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ipopproject.messages.MessageId;

public class Client implements Runnable {
	private final Socket socket;
	private final Server server;
	
	private final int id;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	
	private int port = -1;
	
	/**
	 * Creates an new client
	 * @param socket The socket for the client
	 * @param server The server that the client is associated with
	 */
	public Client(Socket socket, Server server, int id) {
		this.socket = socket;
		this.server = server;
		this.id = id;
	}
	
	/**
	 * Returns the id
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns the IP
	 */
	public String getIP() {
		return this.socket.getInetAddress().getHostAddress();
	}
	
	/**
	 * Returns the port
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Returns the output stream
	 */
	public DataOutputStream getOutputStream() {
		return this.streamOut;
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
				byte messageID = this.streamIn.readByte();
				
				switch (MessageId.fromByte(messageID)) {
				case CONNECT_CLIENT:
					{
						this.port = this.streamIn.readInt();
						int room = this.streamIn.readInt();
						this.server.clientConnected(this, room);
					}
					break;
				default:
					break;
				}
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
	public void close() {
		try {
			if (this.socket != null) {
				this.socket.close();
			}
			
			if (this.streamOut != null) {
				this.streamOut.close();
			}
			
			if (this.streamOut != null) {
				this.streamOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return getId() + "";
	}
}
