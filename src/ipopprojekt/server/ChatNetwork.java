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
	private final Random random = new Random(1337);
	
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
		if (this.clients.size() == 1) {
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
	 * Returns a random client in the network
	 * @return The id of the client
	 */
	public int randomClientInNetwork() {
		return this.clients.get(this.random.nextInt(this.clients.size()));
	}
	
	/**
	 * Creates a set of the given values
	 * @param first The first value
	 * @param others The other values
	 */
	private Set<Integer> createSet(int first, int... others) {
		Set<Integer> set = new HashSet<>();
		
		set.add(first);
		
		for (int x : others) {
			set.add(x);
		}
		
		return set;
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
				changes.add(new Changes(rand, createSet(clientId)));
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
				changes.put(current.getKey(), new Changes(current.getKey(), createSet(clientId)));
			}
		}
		
		//After removing the client, its possible that the network becomes unconnected.
		//So add random connections until the network becomes connected again.		
		while (!this.isConnected()) {
			int from = this.randomClientInNetwork();
			int to = this.randomClient(from, this.neighborList.get(from));
			addEdge(this.neighborList, from, to);
			
			Changes vertexChanges = null;
			if (changes.containsKey(from)) {
				vertexChanges = changes.get(from);
			} else {
				vertexChanges = new Changes(from, createSet(to));
				changes.put(from, vertexChanges);
			}
			
			vertexChanges.clients.add(to);
		}
		
		return new ArrayList<>(changes.values());
	}
}
