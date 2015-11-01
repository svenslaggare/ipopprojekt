package ipopprojekt.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ipopprojekt.ChatMessage;
import ipopprojekt.P2PMessageHandler;

import org.junit.Test;

/**
 * Tests the P2PMessageHandler
 */
public class TestP2PMessageHandler {
	/**
	 * Creates an input stream from the given byte-array backing stream
	 * @param backingStream
	 */
	private DataInputStream fromBackingStream(ByteArrayOutputStream backingStream) {
		return new DataInputStream(new ByteArrayInputStream(backingStream.toByteArray()));
	}
	
	/**
	 * Tests sending and reading a message
	 */
	@Test
	public void testSendAndRead() {
		P2PMessageHandler senderHandler = new P2PMessageHandler(1);
		P2PMessageHandler recieverHandler = new P2PMessageHandler(2);
		recieverHandler.addUser(1, "Anton");
		
		ByteArrayOutputStream backingStream = new ByteArrayOutputStream();
		
		//First msg
		try (DataOutputStream stream = new DataOutputStream(backingStream)) {
			senderHandler.writeMessage(stream, "Hello, World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (DataInputStream stream = fromBackingStream(backingStream)) {
			ChatMessage msg = recieverHandler.nextMessage(stream);
			assertNotNull(msg);
			assertEquals("Anton", msg.getSender());
			assertEquals("Hello, World!", msg.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Second
		backingStream = new ByteArrayOutputStream();
		try (DataOutputStream stream = new DataOutputStream(backingStream)) {
			senderHandler.writeMessage(stream, "Hello, New World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (DataInputStream stream = fromBackingStream(backingStream)) {
			ChatMessage msg = recieverHandler.nextMessage(stream);
			assertNotNull(msg);
			assertEquals("Anton", msg.getSender());
			assertEquals("Hello, New World!", msg.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tests reading a already received message
	 */
	@Test
	public void testReadReceivedMessage() {
		P2PMessageHandler senderHandler = new P2PMessageHandler(1);
		P2PMessageHandler senderHandler2 = new P2PMessageHandler(1);
		P2PMessageHandler recieverHandler = new P2PMessageHandler(2);
		recieverHandler.addUser(1, "Anton");
		
		ByteArrayOutputStream backingStream = new ByteArrayOutputStream();
		
		//Send the same message "twice"
		try (DataOutputStream stream = new DataOutputStream(backingStream)) {
			senderHandler.writeMessage(stream, "Hello, World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		try (DataInputStream stream = fromBackingStream(backingStream)) {
			ChatMessage msg = recieverHandler.nextMessage(stream);
			assertNotNull(msg);
			assertEquals("Anton", msg.getSender());
			assertEquals("Hello, World!", msg.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (DataOutputStream stream = new DataOutputStream(backingStream)) {
			senderHandler2.writeMessage(stream, "Hello, World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		try (DataInputStream stream = fromBackingStream(backingStream)) {
			ChatMessage msg = recieverHandler.nextMessage(stream);
			assertNull(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tests reading a message from a user without a mapping
	 */
	@Test
	public void testReadInvalidUser() {
		P2PMessageHandler senderHandler = new P2PMessageHandler(1);
		P2PMessageHandler recieverHandler = new P2PMessageHandler(2);
		
		ByteArrayOutputStream backingStream = new ByteArrayOutputStream();
		
		//Send the same message "twice"
		try (DataOutputStream stream = new DataOutputStream(backingStream)) {
			senderHandler.writeMessage(stream, "Hello, World!");
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		try (DataInputStream stream = fromBackingStream(backingStream)) {
			ChatMessage msg = recieverHandler.nextMessage(stream);
			assertNull(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
