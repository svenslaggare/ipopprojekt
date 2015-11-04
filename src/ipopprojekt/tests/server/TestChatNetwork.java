package ipopprojekt.tests.server;

import static org.junit.Assert.*;

import java.util.List;

import ipopprojekt.server.ChatNetwork;
import ipopprojekt.server.ChatNetwork.Changes;

import org.junit.Test;

/**
 * Tests the chat network
 */
public class TestChatNetwork {
	/**
	 * Tests that if adding new clients makes the network connected
	 */
	@Test
	public void testConnected() {
		ChatNetwork network = new ChatNetwork(3);
		
		for (int i = 0; i < 1000; i++) {
			network.addClient(i);
			assertTrue(network.isConnected());
			assertTrue(network.exists(i));
		}
	}
	
	/**
	 * Tests that if a client leaves the network that it remains connected
	 */
	@Test
	public void testConnectedIfLeave() {
		ChatNetwork network = new ChatNetwork(3);
		
		for (int i = 0; i < 1000; i++) {
			network.addClient(i);
		}
		
		assertTrue(network.isConnected());

		for (int i = 0; i < 150; i++) {
			int clientId = network.randomClientInNetwork();
			network.removeClient(clientId);
			assertTrue(network.isConnected());
			assertFalse(network.exists(clientId));
		}
	}
	
	/**
	 * Tests a sample network
	 */
	@Test
	public void testSampleNetwork() {
		ChatNetwork network = new ChatNetwork(1, 511);
		network.addClient(0);
		assertTrue(network.isConnected());
		network.addClient(1);
		assertTrue(network.isConnected());
		network.addClient(2);
		assertTrue(network.isConnected());
				
		network.removeClient(1);
		assertTrue(network.isConnected());
	}
}
