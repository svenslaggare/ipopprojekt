package ipopproject.messages;

/**
 * The id of the messages
 */
public enum MessageId {
	SET_USER_ID((byte)0),
	SET_CLIENT_P2P_PORT((byte)1),
	ADD_NEIGHBORS((byte)2),
	REMOVE_NEIGHBORS((byte)3);
	
	private final byte id;
	
	MessageId(byte id) {
		this.id = id;
	}
	
	/**
	 * Returns the id of the message
	 */
	public byte getId() {
		return this.id;
	}
	
	/**
	 * Constructs an object from the given id
	 * @param id The id
	 */
	public static MessageId fromByte(byte id) {
		switch (id) {
		case 0: return SET_USER_ID;
		case 1: return SET_CLIENT_P2P_PORT;
		case 2: return ADD_NEIGHBORS;
		case 3: return REMOVE_NEIGHBORS;
		}
		
		return null;
	}
}