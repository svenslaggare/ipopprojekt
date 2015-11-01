package ipopprojekt.client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * The client GUI for the P2P chat.
 */
public class ClientGUI {
	private static JFrame frame;
	
	private static JPanel roomsPanel;
	private static JRadioButton chatRoom1;
	private static JRadioButton chatRoom2;
	private static JRadioButton chatRoom3;
	private static ButtonGroup chatRooms;
	
	private static JTextField inputField;
	private static JButton sendButton;
	
	private static JTextArea chat;
	private static JScrollPane chatScroll;
	
	private static String name;
	private static int chatRoom = 1;
	
	private static void showConnectBox() {
		roomsPanel = new JPanel();
		roomsPanel.setBounds(5, 8, 320, 50);
		
		chatRoom1 = new JRadioButton("Room 1");
		chatRoom1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (chatRoom != 1) {
					changeChatRoom(1);
				}
			}
		});
		chatRoom1.setSelected(true);
		roomsPanel.add(chatRoom1);
		
		chatRoom2 = new JRadioButton("Room 2");
		chatRoom2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (chatRoom != 2) {
					changeChatRoom(2);
				}
			}
		});
		roomsPanel.add(chatRoom2);
		
		chatRoom3 = new JRadioButton("Room 3");
		chatRoom3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (chatRoom != 3) {
					changeChatRoom(3);
				}
			}
		});
		roomsPanel.add(chatRoom3);
		
		chatRooms = new ButtonGroup();
		chatRooms.add(chatRoom1);
		chatRooms.add(chatRoom2);
		chatRooms.add(chatRoom3);
		
		frame.add(roomsPanel);
		
		inputField.setLocation(5, 58);
		frame.add(inputField);
		
		sendButton.setText("Connect");
		sendButton.setLocation(255, 58);
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Connect
				name = inputField.getText();
				inputField.setText("");
				
				sendButton.removeActionListener(this);
				frame.remove(roomsPanel);
				
				showChatClient();
			}
		});
		frame.add(sendButton);
		
		frame.getRootPane().setDefaultButton(sendButton);
		inputField.requestFocusInWindow();
	}
	
	private static void showChatClient() {
		chat = new JTextArea();
		chat.setLineWrap(true);
		chat.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chat.setEditable(false);
		chatScroll = new JScrollPane(chat, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chatScroll.setBounds(5, 8, 320, 280);
		frame.add(chatScroll);
		
		inputField.setLocation(5, 294);
		frame.add(inputField);
		
		sendButton.setText("Send");
		sendButton.setLocation(255, 294);
		sendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				chat.append(name + ": " + inputField.getText() + "\n");
				
				inputField.setText("");
				inputField.requestFocusInWindow();
			}
		});
		frame.add(sendButton);
		
		frame.getRootPane().setDefaultButton(sendButton);
		inputField.requestFocusInWindow();
	}
	
	private static void changeChatRoom(int room) {
		chatRoom = room;
	}
	
	public static void main(String[] args) {
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
		
		showConnectBox();
		
		frame.setVisible(true);
	}
}