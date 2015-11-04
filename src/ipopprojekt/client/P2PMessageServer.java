package ipopprojekt.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a P2P message server
 */
public class P2PMessageServer {
	private final P2PMessageHandler messageHandler;
	private final P2PMessageReceived messageReceived;
	private final ServerSocket serverSocket;
	private final List<P2PMessageServerClient> clients = new ArrayList<>();
	
	/**
	 * Creates a new P2P message server at the given port
	 * @param messageHandler The message handler
	 * @param messageReceived Handles when a message is received
	 * @param port The port
	 */
	public P2PMessageServer(P2PMessageHandler messageHandler, P2PMessageReceived messageReceived, int port)
			throws IOException {
		this.messageHandler = messageHandler;
		this.messageReceived = messageReceived;
		this.serverSocket = new ServerSocket(port);
		
		//Listen for clients
		Thread acceptThread = new Thread(new Runnable() {		
			@Override
			public void run() {
				while (!serverSocket.isClosed()) {
					try {
						Socket clientSocket = serverSocket.accept();
						
						synchronized (clients) {
							P2PMessageServerClient client = new P2PMessageServerClient(
								clientSocket,
								messageHandler,
								messageReceived);
							
							clients.add(client);
							Thread clientThread = new Thread(client);
							clientThread.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				//Close connections for all clients
				for (P2PMessageServerClient client : clients) {
					client.close();
				}
			}
		});
		acceptThread.start();
	}
	
	/**
	 * Represents a P2P message server client
	 */
	private static class P2PMessageServerClient implements Runnable {
		private final Socket socket;
		private final P2PMessageHandler messageHandler;
		private final P2PMessageReceived messageReceived;
		
		/**
		 * Creates a new message server client
		 * @param socket The socket
		 * @param messageHandler The message handler
		 * @param messageReceived Handles when a message is received
		 */
		public P2PMessageServerClient(
				Socket socket,
				P2PMessageHandler messageHandler,
				P2PMessageReceived messageReceived) {
			this.socket = socket;
			this.messageHandler = messageHandler;
			this.messageReceived = messageReceived;
		}

		@Override
		public void run() {
			try {
				DataInputStream inputStream = new DataInputStream(socket.getInputStream());
				
				while (!this.socket.isClosed()) {
					P2PMessage message = this.messageHandler.nextMessage(inputStream);
					
					synchronized (this.messageReceived) {
						this.messageReceived.received(message);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				this.close();
			}
		}
		
		/**
		 * Closes the connection to the client
		 */
		public void close() {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
