package ipopprojekt.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles P2P messages
 */
public class P2PMessageHandler {
	private final int userId;
	private final String name;
	private final Set<ReceivedMessage> receivedMessages = new HashSet<>();
	private int sequenceNumber = 0;
	
	/**
	 * Creates a new message handler
	 * @param userId The id of the user
	 * @param name The name of the user
	 */
	public P2PMessageHandler(int userId, String name) {
		this.userId = userId;
		this.name = name;
	}
	
	/**
	 * Returns the user id
	 */
	public int getUserId() {
		return this.userId;
	}
	
	/**
	 * Represents a received message
	 */
	private static class ReceivedMessage {
		public final int senderId;
		public final int sequenceNumber;
		
		/**
		 * Creates a new received message
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
	 * Creates a P2P message
	 * @param message The text message
	 */
	public P2PMessage createMessage(String message) {
		synchronized (this) {
			return new P2PMessage(this.userId, this.sequenceNumber++, this.name, message);
		}
	}
	
	/**
	 * Writes the given message to the given stream
	 * @param stream The stream
	 * @param message The message
	 */
	public void writeMessage(DataOutputStream stream, String message) throws IOException {
		this.writeMessage(stream, createMessage(message));
	}
	
	/**
	 * Writes the given message to the given stream
	 * @param stream The stream
	 * @param message The message
	 */
	public void writeMessage(DataOutputStream stream, P2PMessage message) throws IOException {
		stream.writeInt(message.getSenderId());
		stream.writeInt(message.getSequenceNumber());	
		stream.writeUTF(message.getSenderName());
		stream.writeUTF(message.getMessage());
		stream.flush();
	}
	
	/**
	 * Reads the next message from the given stream. If the message has already been read, returns null.
	 * @param stream The input stream
	 * @return The message or null
	 */
	public P2PMessage nextMessage(DataInputStream stream) throws IOException {
		int senderId = stream.readInt();
		int sequenceNumber = stream.readInt();
		String senderName = stream.readUTF();
		String message = stream.readUTF();
		ReceivedMessage receivedMessage = new ReceivedMessage(senderId, sequenceNumber);
		
		//Check if the message has already been received
		synchronized (this) {
			if (!this.receivedMessages.contains(receivedMessage)) {
				this.receivedMessages.add(receivedMessage);
				return new P2PMessage(senderId, sequenceNumber, senderName, message);
			} else {
				return null;
			}
		}
	}
}
