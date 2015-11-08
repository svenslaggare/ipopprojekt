package ipopprojekt.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ipopprojekt.messages.MessageId;
import ipopprojekt.server.ChatNetwork.Change;
import ipopprojekt.server.ChatNetwork.Changes;

/**
 * Represents a chat room.
 */
public class ChatRoom {
	private final int id;
	private final List<Client> clients;
	
	private final ChatNetwork chatNetwork;
	
	/**
	 * Creates a new chat room.
	 * 
	 * @param id The id of the chat room.
	 */
	public ChatRoom(int id) {
		this.id = id;
		this.clients = new ArrayList<>();
		this.chatNetwork = new ChatNetwork(1);
	}
	
	/**
	 * Returns the id of the room.
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Adds a client to the room.
	 * 
	 * @param client The client to add.
	 */
	public void addClient(Client client) {
		clients.add(client);
		
		//Add the client and distribute the changes
		for (Changes changes : this.chatNetwork.addClient(client.getId())) {
			List<Client> toAdd = new ArrayList<>();
			List<Client> toRemove = new ArrayList<>();
			
			for (Change change : changes.getChanges()) {
				switch (change.getType()) {
				case ADD:
					toAdd.add(this.getClient(change.getClientId()));
					break;
				case REMOVE:
					toRemove.add(this.getClient(change.getClientId()));
					break;
				default:
					break;
				}
			}
			
			this.sendAddNeighbors(this.getClient(changes.getClientId()), toAdd);
			this.sendRemoveNeighbors(this.getClient(changes.getClientId()), toRemove);
		}
	}
	
	/**
	 * Removes a client from the room.
	 * 
	 * @param client The client to remove.
	 */
	public boolean removeClient(Client client) {
		if (this.clients.contains(client)) {
			System.out.println("Client: '" + client.toString() + "' removed");
			
			//Remove the client and distribute the changes
			for (Changes changes : this.chatNetwork.removeClient(client.getId())) {
				List<Client> toAdd = new ArrayList<>();
				List<Client> toRemove = new ArrayList<>();
				
				for (Change change : changes.getChanges()) {
					switch (change.getType()) {
					case ADD:
						toAdd.add(this.getClient(change.getClientId()));
						break;
					case REMOVE:
						toRemove.add(this.getClient(change.getClientId()));
						break;
					default:
						break;
					}
				}
				
				this.sendAddNeighbors(this.getClient(changes.getClientId()), toAdd);
				this.sendRemoveNeighbors(this.getClient(changes.getClientId()), toRemove);
			}
			
			this.clients.remove(client);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sends what neighbors to add for the given client
	 * @param client The client
	 * @param toAdd The clients to add
	 */
	private void sendAddNeighbors(Client client, List<Client> toAdd) {
		try {
			if (toAdd.size() > 0) {
				client.getOutputStream().writeByte(MessageId.ADD_NEIGHBORS.getId());		
				this.sendNeighborList(client, toAdd);				
				client.getOutputStream().flush();
			}
		} catch (IOException e) {
			System.err.println("Could not send addNeighbor " + e);
		}
	}
	
	/**
	 * Sends what neighbors to remove for the given client
	 * @param client The client
	 * @param toRemove The clients to remove
	 */
	private void sendRemoveNeighbors(Client client, List<Client> toRemove) {
		try {
			if (toRemove.size() > 0) {
				client.getOutputStream().writeByte(MessageId.REMOVE_NEIGHBORS.getId());		
				
				client.getOutputStream().writeInt(toRemove.size());
				for (Client receiver : toRemove) {
					client.getOutputStream().writeInt(receiver.getId());
				}
				
				client.getOutputStream().flush();
			}
		} catch (IOException e) {
			System.err.println("Could not send addNeighbor " + e);
		}
	}
	
	/**
	 * Sends the given neighbor list to the given client.
	 * Note that this method does not set the type of the message.
	 * @param client The client
	 * @param neighborList The neighbor list
	 */
	private void sendNeighborList(Client client, List<Client> neighborList) {
		try {
			client.getOutputStream().writeInt(neighborList.size());
			
			for (Client receiver : neighborList) {
				client.getOutputStream().writeInt(receiver.getId());
				client.getOutputStream().writeUTF(receiver.getIP());
				client.getOutputStream().writeInt(receiver.getPort());
			}
		} catch (IOException e) {
			System.err.println("Could not send neighbor list: " + e);
		}
	}
	
	/**
	 * Returns clients connected to the room.
	 */
	public List<Client> getClients() {
		return this.clients;
	}
	
	/**
	 * Returns the client with the given id
	 * @param id The id of the client
	 * @return The client or null
	 */
	public Client getClient(int id) {
		for (Client client : this.clients) {
			if (client.getId() == id) {
				return client;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks whether a client is in the room or not.
	 */
	public boolean inRoom(Client client) {
		return clients.contains(client);
	}
	
	/**
	 * Closes a room.
	 */
	public void close() {
		for (Client client : this.clients) {
			client.close();
		}
	}
}
