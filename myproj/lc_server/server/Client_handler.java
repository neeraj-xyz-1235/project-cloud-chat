
import java.io.*;
import java.net.*;

public class Client_handler extends Thread { // Client handler class that extends thread to handle multiple clients at
                                             // the same time
    private Socket CHsocket;
    private PrintWriter spw; // Declared PrintWriter to send messages back to the client

    public Client_handler(Socket socket, PrintWriter spw) { // constructor to initialize the client handler with the
                                                            // incoming socket connection
        CHsocket = socket;
        this.spw = spw;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CHsocket.getInputStream()))) {
            String message;

            while ((message = br.readLine()) != null) {
                String[] parts = message.split("::", 4); // splitting the incoming message into parts using "::" as a
                                                         // delimiter with a limit of 4 to avoid breaking message
                                                         // content

                String command = parts[0]; // the first part of the message is the command

                if (command.equals("LOGIN")) {
                    String username = parts[1];
                    String password = parts[2];

                    System.out.println("Validating user: " + username);
                    Authorization auth = new Authorization(spw);
                    auth.login(username, password);
                } else if (command.equals("REGISTER")) {
                    String username = parts[1];
                    String password = parts[2];

                    System.out.println("Registering user: " + username);
                    Authorization auth = new Authorization(spw);
                    auth.register(username, password);
                } else if (command.equals("CONNECT")) {
                    int currentUserId = Integer.parseInt(parts[1]); // string to int conversion for current user id
                    String targetUser = parts[2];

                    Connection_handler connectionHandler = new Connection_handler(spw);
                    connectionHandler.connectToUser(targetUser, currentUserId);
                } else if (command.equals("MESSAGE")) {
                    int chatRoomId = Integer.parseInt(parts[1]);
                    String sender = parts[2];
                    String content = parts[3];
                    Message_handler messageHandler = new Message_handler(spw);
                    messageHandler.handleMessage(chatRoomId, sender, content);
                } else if (command.equals("CONNECT_ACCEPT")) {
                    String requester = parts[1];
                    // Find the user who sent CONNECT_ACCEPT (we need their username)
                    // But actually we have their spw here. We need to pass the requester to
                    // Connection_handler.
                    Connection_handler connectionHandler = new Connection_handler(spw);
                    connectionHandler.acceptConnection(requester);
                } else if (command.equals("CONNECT_REJECT")) {
                    String requester = parts[1];
                    Connection_handler connectionHandler = new Connection_handler(spw);
                    connectionHandler.rejectConnection(requester);
                } else {
                    System.out.println("Unknown command received: " + command);
                }
            }
        } catch (IOException e) {
            System.out.println("Error occurred while handling client: " + e);
        } finally {
            try {
                if (CHsocket != null && !CHsocket.isClosed()) {
                    CHsocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e);
            }
        }
    }

}
