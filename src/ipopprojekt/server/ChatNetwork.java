package ipopprojekt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Represents a chat network
 */
public class ChatNetwork {
	private final Map<Integer, Set<Integer>> neighborList = new HashMap<>();
	private final int maxNeighborsPerNode;
	private final List<Integer> clients = new ArrayList<>();
	private final Random random = new Random();
	
	/**
	 * Creates a new char network
	 * @param maxNeighborsPerNode The maximum number of neighbors per node
	 */
	public ChatNetwork(int maxNeighborsPerNode) {
		this.maxNeighborsPerNode = maxNeighborsPerNode;
	}
	
	/**
	 * Returns the neighbors for the given client
	 * @param clientId The id of the client
	 */
	public Set<Integer> getNeighbors(int clientId) {
		if (this.neighborList.containsKey(clientId)) {
			return this.neighborList.get(clientId);
		} else {
			return new HashSet<>();
		}
	}
	
	/**
	 * Represents a change to the network that needs to be performed
	 */
	public static class Changes {
		private final int clientId;
		private final Set<Integer> clients;
		
		/**
		 * Creates new changes
		 * @param clientId The id of the client that need to change
		 * @param clients The clients that are changed
		 */
		public Changes(int clientId, Set<Integer> clients) {
			this.clientId = clientId;
			this.clients = clients;
		}
	}
		
	/**
	 * Indicates if the network is connected
	 */
	public boolean isConnected() {
		Set<Integer> visited = new HashSet<>();
		this.visitDFS(visited, -1, this.clients.get(0));
		return visited.size() == this.clients.size();
	}
	
	/**
	 * Visits the neighbors of the current node using DFS
	 * @param visited The already visited
	 * @param parent The parent
	 * @param current The current node
	 */
	private void visitDFS(Set<Integer> visited, int parent, int current) {
		if (visited.contains(current)) {
			return;
		}
		
		visited.add(current);
		
		for (int neighbor : this.neighborList.get(current)) {
			if (parent != current) {
				visitDFS(visited, current, neighbor);
			}
		}
	}
	
	/**
	 * Returns a random client for the given client
	 * @param clientId The id of the client
	 * @param neighbors The neighbors of the client
	 */
	private int randomClient(int clientId, Set<Integer> neighbors) {
		while (true) {
			int id = this.clients.get(this.random.nextInt(this.clients.size()));
			
			if (id != clientId && !neighbors.contains(id)) {
				return id;
			}
		}
	}
	
	/**
	 * Adds the given client to the network
	 * @param clientId The id of the client
	 * @param The changes that need to be sent to the clients
	 */
	public List<Changes> addClient(int clientId) {
		this.clients.add(clientId);
		Set<Integer> clientList = new HashSet<>();
		this.neighborList.put(clientId, clientList);
		
		List<Changes> changes = new ArrayList<>();
		
		if (this.clients.size() > 1) {			
			//Start by adding maxNum random nodes for the client
			for (int i = 0; i < Math.min(this.maxNeighborsPerNode, this.clients.size() - 1); i++) {
				clientList.add(this.randomClient(clientId, clientList));
			}
			
			changes.add(new Changes(clientId, clientList));
			
			//Then add clients that has the new client as a neighbor
			Set<Integer> added = new HashSet<>();
			for (int i = 0; i < Math.min(this.maxNeighborsPerNode, this.clients.size() - 1); i++) {
				int rand = this.randomClient(clientId, added);
				added.add(rand);
				this.neighborList.get(rand).add(clientId);
				changes.add(new Changes(rand, new HashSet<>(Collections.singleton(clientId))));
			}
		}
		
		return changes;
	}
}
