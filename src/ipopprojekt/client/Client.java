package ipopprojekt.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import ipopprojekt.server.MessageID;

public class Client implements Runnable {
	private String name;
	private int chatRoom;
	
	private String serverName;
	private int serverPort;
	
	private Socket clientSocket;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	
	public Client(String name, int chatRoom) {
		this.name = name;
		this.chatRoom = chatRoom;
		
		connect("localhost", 4711);
	}
	
	public String getName() {
		return name;
	}
	
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
		} catch (IOException ioe) {
			
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
			
			// TODO: Skicka meddelande om namn till servern
			this.streamOut.writeByte(MessageID.SET_NAME.getId());
			this.streamOut.writeUTF(getName());
			
			System.out.println("Connected to server: " + this.serverName + ":" + this.serverPort);	
			
			//Handle communication in a separate thread
			Thread clientThread = new Thread(this);
			clientThread.start();
			
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
				int commandSize = this.streamIn.readInt();
//				short numberOfCommands = this.streamIn.readShort();
				
				//Read the command buffer
//				byte[] commandData = new byte[commandSize];				
//				this.streamIn.read(commandData, 0, commandSize);
//				ByteBuffer commandBuffer = ByteBuffer.wrap(commandData);
				
				//Enqueue the commands
//				for (short i = 0; i < numberOfCommands; i++) {
//					NetworkCommand command = NetworkCommand.readCommandFromBuffer(commandBuffer);
//					this.networkCommandManager.enqueueCommand(command);
//				}
				
				//Execute the commands if the commands isn't batched
//				if (!this.batchCommands) {
//					this.networkCommandManager.executeCommandBuffer();
//				}
			} catch (IOException e) {
				this.disconnect();
				break;
			}
		}
	}
	
	/**
	 * Sends an command to the server
	 * @param command The command to send
	 * @throws IOException If an IO exception happens
	 */
	/*public void sendCommand(NetworkCommand command) throws IOException {
		if (this.isConnected()) {
			byte[] commandData = command.generateCommandBuffer();
			this.streamOut.writeInt(commandData.length);	//The size command data
			this.streamOut.writeShort(1);					//The number of commands
			this.streamOut.write(commandData);				//The command data
			this.streamOut.flush();							//Send the data
		}
	}*/
}
