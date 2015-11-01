package ipopprojekt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles P2P messages
 */
public class P2PMessageHandler {
	private final int userId;
	private final Set<ReceivedMessage> receivedMessages = new HashSet<>();
	private final Map<Integer, String> users = new HashMap<>();
	private int sequenceNumber = 0;
	
	/**
	 * Creates a new message handler
	 * @param userId The id of the user
	 */
	public P2PMessageHandler(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Represents a received message
	 */
	private static class ReceivedMessage {
		public final int senderId;
		public final int sequenceNumber;
		
		/**
		 * Creates a new recieved message
		 */
		public ReceivedMessage(int senderId, int sequenceNumber) {
			this.senderId = senderId;
			this.sequenceNumber = sequenceNumber;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + senderId;
			result = prime * result + sequenceNumber;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReceivedMessage other = (ReceivedMessage) obj;
			if (senderId != other.senderId)
				return false;
			if (sequenceNumber != other.sequenceNumber)
				return false;
			return true;
		}
	}

	/**
	 * Adds the given user to the list of users
	 * @param id The id of the user
	 * @param name The name of the user
	 */
	public void addUser(int id, String name) {
		this.users.put(id, name);
	}
	
	/**
	 * Removes the user with the given id
	 * @param id The id of the user
	 */
	public void removeUser(int id) {
		this.users.remove(id);
	}
	
	/**
	 * Writes the given message to the given stream
	 * @param stream The stream
	 * @param message The message
	 */
	public void writeMessage(DataOutputStream stream, String message) throws IOException {
		stream.writeInt(this.userId);
		
		synchronized (this) {
			stream.writeInt(this.sequenceNumber++);
		}
		
		stream.writeUTF(message);
		stream.flush();
	}
	
	/**
	 * Reads the next message from the given stream. If the message has already been read, returns null.
	 * @param stream The input stream
	 * @return The message or null
	 */
	public ChatMessage nextMessage(DataInputStream stream) throws IOException {
		int senderId = stream.readInt();
		int sequenceNumber = stream.readInt();
		String message = stream.readUTF();
		ReceivedMessage receivedMessage = new ReceivedMessage(senderId, sequenceNumber);
		
		//Check if the message has been received
		synchronized (this) {
			if (!this.receivedMessages.contains(receivedMessage)) {
				this.receivedMessages.add(receivedMessage);
				
				if (!this.users.containsKey(senderId)) {
					return null;
				}
				
				String sender = this.users.get(senderId);
				return new ChatMessage(LocalDateTime.now(), sender, message);
			} else {
				return null;
			}
		}
	}
}
