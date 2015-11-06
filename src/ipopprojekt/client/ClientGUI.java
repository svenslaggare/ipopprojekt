package ipopprojekt.client;

import java.awt.event.*;
import java.time.LocalDateTime;

import javax.swing.*;

/**
 * The GUI for the P2P chat client.
 */
public class ClientGUI {
	private JFrame frame;
	
	private JPanel connectPanel;
	private JLabel serverNameLabel;
	private JTextField serverName;
	private JLabel serverPortLabel;
	private JTextField serverPort;
	private JLabel userNameLabel;
	private JTextField userName;
	private JButton connectButton;
	private JLabel errorLabel;
	
	private JPanel chatRoomPanel;
	private JLabel chatRoomLabel;
	private JSpinner chatRoomSelector;
	private JButton joinRoomButton;
	
	private JPanel chatPanel;
	private JTextField inputField;
	private JButton sendButton;	
	private JTextArea chat;
	private JScrollPane chatScroll;
	
	private NetworkClient client;
	
	/**
	 * Creates a new GUI
	 */
	public ClientGUI() {
		this.create();	
		this.showConnectScreen();
	}
	
	/**
	 * Creates the GUI
	 */
	private void create() {
		//The frame
		this.frame = new JFrame("P2P Chat");
		this.frame.setLayout(null);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(336, 350);
		this.frame.setResizable(false);
		this.frame.setVisible(true);	
		
		//The connect screen
		int marginLeft = 10;
		final int deltaY = 25;
		int posY = 5;
		
		this.connectPanel = new JPanel(null);
		this.connectPanel.setVisible(false);
		this.connectPanel.setSize(336, 350);
		this.frame.add(this.connectPanel);
		
		this.serverNameLabel = new JLabel("Server hostname:");
		this.serverNameLabel.setBounds(marginLeft, posY, 130, 20);
		this.connectPanel.add(this.serverNameLabel);
		
		this.serverName = new JTextField("localhost");
		this.serverName.setBounds(marginLeft + 140, posY, 125, 20);
		this.connectPanel.add(this.serverName);
		
		this.serverPortLabel = new JLabel("Server port:");
		this.serverPortLabel.setBounds(marginLeft, posY += deltaY, 130, 20);
		this.connectPanel.add(this.serverPortLabel);
		
		this.serverPort = new JTextField("4711");
		this.serverPort.setBounds(marginLeft + 140, posY, 50, 20);
		this.connectPanel.add(this.serverPort);
		
		this.userNameLabel = new JLabel("User name:");
		this.userNameLabel.setBounds(marginLeft, posY += deltaY, 100, 20);
		this.connectPanel.add(this.userNameLabel);
		
		this.userName = new JTextField("");
		this.userName.setBounds(marginLeft + 140, posY, 120, 20);
		this.connectPanel.add(this.userName);
		
		this.connectButton = new JButton("Connect");
		this.connectButton.setBounds(marginLeft + 100, posY += deltaY + 10, 120, 20);
		this.connectPanel.add(this.connectButton);
		this.connectButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = userName.getText();
				
				if (!name.equals("")) { 
					errorLabel.setVisible(false);
					String hostname = serverName.getText();
					int port = Integer.parseInt(serverPort.getText());
					
					client = new NetworkClient(hostname, port, name, new ChatMessageReceived() {			
						@Override
						public void received(ChatMessage message) {
							chat.append(message + "\n");
						}
					}, new ChatRoomListReceived() {
						@Override
						public void listReceived(int numRooms) {
							SpinnerNumberModel model = (SpinnerNumberModel)chatRoomSelector.getModel();
							model.setMaximum(numRooms);
						}
					}, new ConnectionEvents() {		
						@Override
						public void failedToConnect() {
							errorLabel.setText("Could not connect to server.");
							errorLabel.setVisible(true);
						}
						
						@Override
						public void disconnected() {
							showConnectScreen();
							errorLabel.setText("Disconnected from server");
							errorLabel.setVisible(true);
						}
						
						@Override
						public void connected() {
							showJoinRoomScreen();
						}
					});
				}
			}
		});
		
		this.errorLabel = new JLabel();
		this.errorLabel.setBounds(marginLeft, posY += deltaY, 330, 20);
		this.errorLabel.setVisible(false);
		this.connectPanel.add(this.errorLabel);
		
		//The join chat room screen
		posY = 5;
		marginLeft = 100;
		this.chatRoomPanel = new JPanel(null);
		this.chatRoomPanel.setVisible(false);
		this.chatRoomPanel.setSize(336, 350);
		this.frame.add(this.chatRoomPanel);
		
		this.chatRoomLabel = new JLabel("Room:");
		this.chatRoomLabel.setBounds(marginLeft, posY, 130, 20);
		this.chatRoomPanel.add(this.chatRoomLabel);
		
		this.chatRoomSelector = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
		this.chatRoomSelector.setBounds(marginLeft + 50, posY, 70, 20);
		this.chatRoomPanel.add(this.chatRoomSelector);
		
		this.joinRoomButton = new JButton("Join");
		this.joinRoomButton.setBounds(marginLeft + 10, posY + deltaY + 10, 120, 20);
		this.chatRoomPanel.add(this.joinRoomButton);
		this.joinRoomButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				showChatScreen();
				client.connect((int)chatRoomSelector.getValue());
			}
		});
		
		//Chat screen
		int chatWidth = 320;
		
		this.chatPanel = new JPanel(null);
		this.chatPanel.setVisible(false);
		this.chatPanel.setSize(336, 350);
		this.frame.add(this.chatPanel);
		
		this.chat = new JTextArea();
		this.chat.setLineWrap(true);
		this.chat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.chat.setEditable(false);
		this.chatScroll = new JScrollPane(
			this.chat,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.chatScroll.setBounds(5, 8, chatWidth, 280);
		this.chatPanel.add(chatScroll);
		
		this.inputField = new JTextField();
		this.inputField.setBounds(5, 294, chatWidth - 90, 20);
		this.chatPanel.add(inputField);
		
		this.sendButton = new JButton("Send");
		this.sendButton.setBounds(245, 294, 80, 20);
		this.sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				ChatMessage chatMsg = new ChatMessage(
					LocalDateTime.now(),
					client.getName(),
					inputField.getText());
				
				chat.append(chatMsg.toString() + "\n");	
				client.sendMessage(inputField.getText());
				
				inputField.setText("");
				inputField.requestFocusInWindow();
			}
		});
		this.chatPanel.add(sendButton);
		
		this.frame.getRootPane().setDefaultButton(this.sendButton);
		this.inputField.requestFocusInWindow();
	}
	
	/**
	 * Shows the connect screen
	 */
	private void showConnectScreen() {
		this.connectPanel.setVisible(true);
		this.chatRoomPanel.setVisible(false);
		this.chatPanel.setVisible(false);
	}
	
	/**
	 * Shows the join chat room screen
	 */
	private void showJoinRoomScreen() {
		this.chatRoomPanel.setVisible(true);
		this.connectPanel.setVisible(false);
		this.chatPanel.setVisible(false);
	}
	
	/**
	 * Shows the chat screen
	 */
	private void showChatScreen() {
		this.chatPanel.setVisible(true);
		this.connectPanel.setVisible(false);
		this.chatRoomPanel.setVisible(false);
	}
	
	
	/**
	 * Main entry point
	 */
	public static void main(String[] args) {
		 SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	new ClientGUI();
            }
        });
	}
}