package ipopprojekt.client;

/**
 * Handles when the list of chat rooms are received.
 */
public interface ChatroomListReceived {
	/**
	 * The list is received.
	 * 
	 * @param numRooms The number of rooms.
	 */
	void listReceived(int numRooms);
}
