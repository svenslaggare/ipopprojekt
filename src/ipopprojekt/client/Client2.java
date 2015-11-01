package ipopprojekt.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client2 {
	public static void main(String[] args) {
		try {
			P2PClient client = new P2PClient(4712, 2, new ChatMessageReceived() {
				@Override
				public void received(ChatMessage message) {
					System.out.println(message);
				}
			});
			
			client.addUser(1, "Client 1");
			client.addUser(3, "Client 3");
			
			client.addNeighbor(3, new InetSocketAddress(InetAddress.getLocalHost(), 4713));
			
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					String input = scanner.nextLine();
					client.send(input);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
