package ipopprojekt.client;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;

import javax.swing.*;

/**
 * The client GUI for the P2P chat.
 */
public class ClientGUI {
	private JFrame frame;
	
	private JPanel roomsPanel;
	private JLabel roomLabel;
	private JSpinner chatRoom;
	
	private JTextField inputField;
	private JButton sendButton;
	
	private JTextArea chat;
	private JScrollPane chatScroll;
	
	private NetworkClient client;
	
	/**
	 * Creates a new GUI
	 */
	public ClientGUI() {
		createBase();	
		frame.setVisible(true);	
		
		client = new NetworkClient(new ChatMessageReceived() {			
			@Override
			public void received(ChatMessage message) {
				chat.append(message + "\n");
			}
		}, new ChatRoomListReceived() {
			@Override
			public void listReceived(int numRooms) {
				SpinnerNumberModel model = (SpinnerNumberModel)chatRoom.getModel();
				model.setMaximum(numRooms);
			}
		}, new ConnectionEvents() {		
			@Override
			public void failedToConnect() {
				JLabel failText = new JLabel("Could not connect to server.");
				failText.setBounds(50, 5, 250, 20);
				frame.add(failText);
				frame.setVisible(true);
			}
			
			@Override
			public void disconnected() {
				roomsPanel.setVisible(false);
				inputField.setVisible(false);
				sendButton.setVisible(false);
				
				if (chat != null) {
					chat.setVisible(false);
					chatScroll.setVisible(false);
				}
				
				JLabel failText = new JLabel("Disconnected from server.");
				failText.setBounds(70, 5, 250, 20);
				frame.add(failText);
				frame.setVisible(true);
			}
			
			@Override
			public void connected() {
				showConnectBox();
			}
		});
	}
	
	/**
	 * Creates the base GUI
	 */
	private void createBase() {
		frame = new JFrame("P2P Chat");
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(336, 350);
		frame.setResizable(false);
		
		inputField = new JTextField();
		inputField.setSize(250, 20);
		
		sendButton = new JButton();
		sendButton.setSize(70, 20);
		sendButton.setMargin(new Insets(0, 0, 0, 0));
	}
	
	/**
	 * Displays the connection box
	 */
	private void showConnectBox() {
		roomsPanel = new JPanel();
		roomsPanel.setBounds(5, 8, 320, 50);
		
		roomLabel = new JLabel("Room:");
		roomsPanel.add(roomLabel);
		
		chatRoom = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
		roomsPanel.add(chatRoom);
		
		frame.add(roomsPanel);
		
		inputField.setLocation(5, 58);
		frame.add(inputField);
		
		sendButton.setText("Connect");
		sendButton.setLocation(255, 58);
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String name = inputField.getText().trim();
				
				if (!name.isEmpty()) {
					// Connect
					int room = (int)chatRoom.getValue();
					
					client.setName(name);
					client.connect(room);
					
					inputField.setText("");
					
					sendButton.removeActionListener(this);
					frame.remove(roomsPanel);
					
					frame.setTitle("P2P Chat [Room " + room + "] - " + name);
					
					showChatClient();
				}
			}
		});
		frame.add(sendButton);
		
		frame.getRootPane().setDefaultButton(sendButton);
		inputField.requestFocusInWindow();
		frame.setVisible(true);
	}
	
	/**
	 * Shows the chat client
	 */
	private void showChatClient() {
		chat = new JTextArea();
		chat.setLineWrap(true);
		chat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chat.setEditable(false);
		chatScroll = new JScrollPane(
			chat,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		chatScroll.setBounds(5, 8, 320, 280);
		frame.add(chatScroll);
		
		inputField.setLocation(5, 294);
		frame.add(inputField);
		
		sendButton.setText("Send");
		sendButton.setLocation(255, 294);
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				chat.append(new ChatMessage(LocalDateTime.now(), client.getName(), inputField.getText()).toString() + "\n");	
				client.sendMessage(inputField.getText());
				
				inputField.setText("");
				inputField.requestFocusInWindow();
			}
		});
		frame.add(sendButton);
		
		frame.getRootPane().setDefaultButton(sendButton);
		inputField.requestFocusInWindow();
	}
	
	/**
	 * Main entry point
	 */
	public static void main(String[] args) {
		new ClientGUI();
	}
}