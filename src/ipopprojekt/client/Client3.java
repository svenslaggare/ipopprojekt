package ipopprojekt.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client3 {
	public static void main(String[] args) {
		try {
			P2PClient client = new P2PClient(4713, 3, "Client 3", new ChatMessageReceived() {
				@Override
				public void received(ChatMessage message) {
					System.out.println(message);
				}
			});
						
			client.addNeighbor(1, new InetSocketAddress(InetAddress.getLocalHost(), 4711));
			
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
