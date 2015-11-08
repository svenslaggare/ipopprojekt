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
	private final Random random;
	
	/**
	 * Creates a new char network
	 * @param maxNeighborsPerNode The maximum number of neighbors per node
	 * @param seed The seed to use for generating random neighbors
	 */
	public ChatNetwork(int maxNeighborsPerNode, long seed) {
		this.maxNeighborsPerNode = maxNeighborsPerNode;
		this.random = new Random(seed);
	}
	
	/**
	 * Creates a new char network
	 * @param maxNeighborsPerNode The maximum number of neighbors per node
	 */
	public ChatNetwork(int maxNeighborsPerNode) {
		this(maxNeighborsPerNode, System.currentTimeMillis());
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
	 * The change types
	 */
	public static enum ChangeType {
		ADD,
		REMOVE
	}
	
	/**
	 * Represents a change
	 */
	public static class Change {
		private final int clientId;
		private final ChangeType type;
		
		/**
		 * Creates a new change
		 * @param clientId The user affected by the change
		 * @param type The type
		 */
		public Change(int clientId, ChangeType type) {
			this.clientId = clientId;
			this.type = type;
		}

		/**
		 * Returns the client id
		 */
		public int getClientId() {
			return clientId;
		}

		/**
		 * Returns the change type
		 */
		public ChangeType getType() {
			return type;
		}		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + clientId;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			Change other = (Change) obj;
			if (clientId != other.clientId)
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}
	
	/**
	 * Represents a change to the network that needs to be performed
	 */
	public static class Changes {
		private final int clientId;
		private final Set<Change> changes;
		
		/**
		 * Creates new changes
		 * @param clientId The id of the client that need to change
		 * @param changes The clients that are changed
		 */
		public Changes(int clientId, Set<Change> changes) {
			this.clientId = clientId;
			this.changes = changes;
		}

		/**
		 * Returns the client id
		 */
		public int getClientId() {
			return clientId;
		}

		/**
		 * Returns the changes
		 * @return
		 */
		public Set<Change> getChanges() {
			return changes;
		}
	}
			
	/**
	 * Adds an edge in the given graph
	 * @param graph The graph
	 * @param from The start point
	 * @param to The end point
	 */
	private static void addEdge(Map<Integer, Set<Integer>> graph, int from, int to) {
		Set<Integer> neighborList = null;
		if (graph.containsKey(from)) {
			neighborList = graph.get(from);
		} else {
			neighborList = new HashSet<>();
			graph.put(from, neighborList);
		}
		
		neighborList.add(to);
	}
	
	/**
	 * Creates a reversed graph
	 */
	private Map<Integer, Set<Integer>> createReverseGraph() {
		Map<Integer, Set<Integer>> reverseEdges = new HashMap<>();
				
		for (Map.Entry<Integer, Set<Integer>> current : neighborList.entrySet()) {
			reverseEdges.put(current.getKey(), new HashSet<>());
			
			int from = current.getKey();
			for (int to : current.getValue()) {
				addEdge(reverseEdges, to, from);
			}
		}
		
		return reverseEdges;
	}
	
	/**
	 * Indicates if the network is connected
	 */
	public boolean isConnected() {
		if (this.clients.size() <= 1) {
			return true;
		}
		
		Set<Integer> visited = new HashSet<>();
		int v = this.clients.get(0);
		this.visitDFS(this.neighborList, visited, v);
		
		if (visited.size() == this.clients.size()) {
			//We now know that vertex v can reach every other vertex.
			//Now check that all other vertex can reach v
			visited.clear();
			visitDFS(this.createReverseGraph(), visited, v);
			return visited.size() == this.clients.size();
		} else {
			return false;
		}
	}
	
	/**
	 * Visits the neighbors of the current node using DFS
	 * @param graph The current graph
	 * @param visited The already visited
	 * @param current The current
	 */
	private void visitDFS(Map<Integer, Set<Integer>> graph, Set<Integer> visited, int current) {
		if (visited.contains(current)) {
			return;
		}
		
		visited.add(current);
		
		for (int neighbor : graph.get(current)) {
			visitDFS(graph,visited, neighbor);
		}
	}
	
	/**
	 * Returns a random client for the given client
	 * @param clientId The id of the client
	 * @param neighbors The neighbors of the client
	 * @return The random client or -1 if there are none
	 */
	private int randomClient(int clientId, Set<Integer> neighbors) {
		//Check that there can be any random clients
		if (neighbors.size() == this.clients.size() - 1) {
			return -1;
		}
		
		while (true) {
			int id = this.clients.get(this.random.nextInt(this.clients.size()));
			
			if (id != clientId && !neighbors.contains(id)) {
				return id;
			}
		}
	}
	
	/**
	 * Returns a random client in the network
	 * @return The id of the client
	 */
	public int randomClientInNetwork() {
		return this.clients.get(this.random.nextInt(this.clients.size()));
	}
	
	/**
	 * Indicates if the given client exists in the network
	 * @param clientId The id of the client
	 */
	public boolean exists(int clientId) {
		return this.neighborList.containsKey(clientId);
	}
		
	/**
	 * Makes the network connected again
	 * @param changes  The changes that needs to be made
	 */
	private void makeConnected(Map<Integer, Changes> changes) {
		while (!this.isConnected()) {
			int from = this.randomClientInNetwork();
			
			//Check that there is any edge to add
			if (this.neighborList.get(from).size() < this.clients.size() - 1) {
				int to = this.randomClient(from, this.neighborList.get(from));
				addEdge(this.neighborList, from, to);
				
				Changes vertexChanges = null;
				if (changes.containsKey(from)) {
					vertexChanges = changes.get(from);
				} else {
					Set<Change> clientChanges = new HashSet<>();
					vertexChanges = new Changes(from, clientChanges);
					changes.put(from, vertexChanges);
				}
				
				vertexChanges.changes.add(new Change(to, ChangeType.ADD));		
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
			Set<Change> clientChanges = new HashSet<>();
			
			//Start by adding maxNum random nodes for the client
			for (int i = 0; i < Math.min(this.maxNeighborsPerNode, this.clients.size() - 1); i++) {
				int rand = this.randomClient(clientId, clientList);
				clientList.add(rand);
				clientChanges.add(new Change(rand, ChangeType.ADD));
			}
			
			changes.add(new Changes(clientId, clientChanges));
			
			//Then add clients that has the new client as a neighbor
			Set<Integer> added = new HashSet<>();
			for (int i = 0; i < Math.min(this.maxNeighborsPerNode, this.clients.size() - 1); i++) {
				int rand = this.randomClient(clientId, added);
				added.add(rand);
				this.neighborList.get(rand).add(clientId);
				
				changes.add(new Changes(rand, Collections.singleton(new Change(clientId, ChangeType.ADD))));
			}
		}
				
		return changes;
	}
	
	/**
	 * Removes the given client from the network
	 * @param clientId The id of the client
	 * @return The changes that need to be sent to the clients
	 */
	public List<Changes> removeClient(int clientId) {
		Map<Integer, Changes> changes = new HashMap<>();
		
		//Remove the client
		this.clients.remove((Integer)clientId);
		this.neighborList.remove(clientId);
		
		//Now all connections to it
		for (Map.Entry<Integer, Set<Integer>> current : this.neighborList.entrySet()) {
			if (current.getValue().remove(clientId)) {
				Set<Change> clientChanges = new HashSet<>();
				clientChanges.add(new Change(clientId, ChangeType.REMOVE));
				changes.put(current.getKey(), new Changes(current.getKey(), clientChanges));
			}
		}
			
		if (this.clients.size() > 0) {
			//After removing the client, its possible that the network becomes unconnected.
			//So add random connections until the network becomes connected again.	
			this.makeConnected(changes);
		}
		
		return new ArrayList<>(changes.values());
	}
	
	@Override
	public String toString() {
		return this.neighborList.toString();
	}
}
