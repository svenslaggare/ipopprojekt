package ipopprojekt.tests.server;

import static org.junit.Assert.*;
import ipopprojekt.server.ChatNetwork;

import org.junit.Test;

/**
 * Tests the chat network
 */
public class TestChatNetwork {
	@Test
	public void test() {
		ChatNetwork network = new ChatNetwork(3);
		
		for (int i = 0; i < 8; i++) {
			network.addClient(i);
		}
		
		System.out.println(network.isConnected());
	}
}
