package Services;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Messenger extends JFrame {

    private String current_user;
    private int current_user_id;
    private String target_user;
    private int target_user_id;
    private int chat_room_id;
    PrintWriter pw; // Declared PrintWriter to send messages to the server
    BufferedReader br; // Declared BufferedReader to read messages from the server

    // Connection Components
    private JTextField connectUserInput;
    private JButton connectButton;

    // Chat Components
    private JTextPane chatHistory;
    private JTextField messageInput;
    private JButton sendButton;

    public Messenger(PrintWriter pw, BufferedReader br) {

        this.pw = pw;
        this.br = br;

        // Initialize current user info here so it's available even if they don't initiate the connection
        this.current_user = User_auth.getCurrentUser();
        this.current_user_id = User_auth.getCurrentUserId();

        // Window setup
        setTitle("LAN Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null); // Centres the window

        // Set the main layout with some spacing between components
        setLayout(new BorderLayout(10, 10));

        // Add padding around the edges of the main window
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP PANEL: Connect to User ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Left-aligned with horizontal gap

        JLabel connectLabel = new JLabel("Connect to User:");
        connectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        connectUserInput = new JTextField(15); // Width for 15 columns
        connectUserInput.setFont(new Font("SansSerif", Font.PLAIN, 14));

        connectButton = new JButton("Connect");
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Attach action listener for connecting
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleConnect();
            }
        });

        topPanel.add(connectLabel);
        topPanel.add(connectUserInput);
        topPanel.add(connectButton);

        // Add the top panel to the main window
        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Chat History ---
        chatHistory = new JTextPane();
        chatHistory.setEditable(false); // Make it read-only
        chatHistory.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Wrap the text area in a scroll pane
        JScrollPane scrollPane = new JScrollPane(chatHistory);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        // --- BOTTOM PANEL: Message Input ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout(10, 0)); // 10px horizontal gap

        messageInput = new JTextField();
        messageInput.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Attach action listener so pressing 'Enter' inside the text field triggers
        // send
        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Attach action listener for sending messages
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendMessage();
            }
        });

        bottomPanel.add(messageInput, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Add the bottom panel to the main window
        add(bottomPanel, BorderLayout.SOUTH);

        // Start the background receiver thread to listen for server messages
        Receiver receiver = new Receiver(br, this);
        new Thread(receiver::startReceiving).start();
    }

    private void handleConnect() {
        if (connectUserInput.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username to connect.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            String targetUser = connectUserInput.getText().trim();

            // Send connect request to the server.
            String payload = "CONNECT::" + current_user_id + "::" + targetUser + "::" + current_user_id;
            pw.println("ENCRYPTED::" + User_auth.getCurrentUser() + "::" + CryptUtil.encrypt(payload, CryptUtil.sessionKey));
        }
    }

    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message to send.", "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } else {
            String payload = "MESSAGE::" + chat_room_id + "::" + current_user_id + "::" + message;
            pw.println("ENCRYPTED::" + User_auth.getCurrentUser() + "::" + CryptUtil.encrypt(payload, CryptUtil.sessionKey));
            //display the sent message in the chat history
            appendChatMessage(current_user != null ? current_user : "Me", message);
            
            messageInput.setText(""); // Clear the input field after sending
        }
    }

    private void appendChatMessage(String username, String message) {
        try {
            javax.swing.text.StyledDocument doc = chatHistory.getStyledDocument();
            
            javax.swing.text.Style userStyle = chatHistory.addStyle("UserStyle", null);
            javax.swing.text.StyleConstants.setForeground(userStyle, Color.RED);
            javax.swing.text.StyleConstants.setBold(userStyle, true);
            
            javax.swing.text.Style messageStyle = chatHistory.addStyle("MessageStyle", null);
            javax.swing.text.StyleConstants.setForeground(messageStyle, Color.BLACK);
            
            doc.insertString(doc.getLength(), username + ": ", userStyle);
            doc.insertString(doc.getLength(), message + "\n", messageStyle);
            
            chatHistory.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Callback invoked by Receiver to handle CONNECT response
    public void handleConnectResponse(String responseType, int chatRoomId) {
        String targetUser = connectUserInput.getText().trim();
        SwingUtilities.invokeLater(() -> {
            if (responseType.equals("SUCCESS")) {
                JOptionPane.showMessageDialog(this,
                        "Connected to " + targetUser + " successfully!", "Connection Success",
                        JOptionPane.INFORMATION_MESSAGE);
                chat_room_id = chatRoomId; //gets chatroomid
            } else if (responseType.equals("USER_NOT_FOUND")) {
                JOptionPane.showMessageDialog(this, "User " + targetUser + " not found.",
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                        
            } else if (responseType.equals("ALREADY_CONNECTED")) {
                JOptionPane.showMessageDialog(this,
                        "You are already connected to " + targetUser + ".", "Connection Info",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (responseType.equals("REJECTED")) {
                JOptionPane.showMessageDialog(this,
                        "Connection request was rejected by " + targetUser + ".", "Connection Rejected",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Callback invoked by Receiver to handle incoming chat messages
    public void handleIncomingMessage(String sender, String content) {
        SwingUtilities.invokeLater(() -> {
            appendChatMessage(sender, content);
        });
    }

    // Callback invoked by Receiver when another user requests to connect (ai coded)
    public void handleIncomingConnectionRequest(String requester) {
        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(this, // it used int option here cause, joption dialog returns an
                                                             // int value
                    requester + " wants to connect with you. Accept?",
                    "Connection Request",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                String payload = "CONNECT_ACCEPT::" + requester;
                pw.println("ENCRYPTED::" + User_auth.getCurrentUser() + "::" + CryptUtil.encrypt(payload, CryptUtil.sessionKey));
            } else {
                String payload = "CONNECT_REJECT::" + requester;
                pw.println("ENCRYPTED::" + User_auth.getCurrentUser() + "::" + CryptUtil.encrypt(payload, CryptUtil.sessionKey));
            }
        });
    }
}