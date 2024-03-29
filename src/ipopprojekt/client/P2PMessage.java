package ipopprojekt.client;

/**
 * Represents a P2P message
 */
public class P2PMessage {
	private final int senderId;
	private final int sequenceNumber;
	private final String senderName;
	private final String message;
	
	/**
	 * Creates a new P2P message
	 * @param senderId The sender id
	 * @param sequenceNumber The sequence number
	 * @param senderName The name of the sender
	 * @param message The message
	 */
	public P2PMessage(int senderId, int sequenceNumber, String senderName, String message) {
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.senderName = senderName;
		this.message = message;
	}

	/**
	 * Returns the sender id
	 */
	public int getSenderId() {
		return senderId;
	}

	/**
	 * Returns the sequence number
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	/**
	 * Returns the name of the sender
	 */
	public String getSenderName() {
		return senderName;
	}

	/**
	 * Returns the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + senderId;
		result = prime * result
				+ ((senderName == null) ? 0 : senderName.hashCode());
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
		P2PMessage other = (P2PMessage) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (senderId != other.senderId)
			return false;
		if (senderName == null) {
			if (other.senderName != null)
				return false;
		} else if (!senderName.equals(other.senderName))
			return false;
		if (sequenceNumber != other.sequenceNumber)
			return false;
		return true;
	}
}
