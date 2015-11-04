package ipopprojekt.server;

public enum MessageID {
	SET_NAME((byte)0),
	GET_LIST((byte)1);
	
	private final byte id;
	
	MessageID(byte id) {
			this.id = id;
	}
	
	public byte getId() {
		return this.id;
	}
	
	public static MessageID fromByte(byte id) {
		switch (id) {
		case 0: return SET_NAME;
		case 1: return GET_LIST;
		}
		
		return null;
	}
}