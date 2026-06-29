//I need this class so that i will receive packets from the server in real-time instead of waiting for the user to send a message to receive packets from the server. 

package Services;

import java.io.*;

public class Receiver {
    private BufferedReader br;
    private Messenger messenger;

    public Receiver(BufferedReader br, Messenger messenger) {
        this.br = br;
        this.messenger = messenger;
    }

    public void startReceiving() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                String[] messageParts = message.split("::", -1);
                String command = messageParts[0];

                if (command.equals("MESSAGE")) {
                    if (messageParts.length >= 3) {
                        String sender = messageParts[1];
                        String content = messageParts[2];
                        messenger.handleIncomingMessage(sender, content);
                    } else if (messageParts.length == 2) {
                        messenger.handleIncomingMessage(messageParts[1], "");
                    } else {
                        System.out.println("Received malformed MESSAGE: " + message);
                    }
                } else if (command.equals("CONNECT")) {
                    String responseType = messageParts[1];
                    int chatRoomId = Integer.parseInt(messageParts[2]);
                    String targetUser = messageParts[3];
                    String current_user = messageParts[4];

                    messenger.handleConnectResponse(responseType, chatRoomId);
                } else if (command.equals("CONNECT_REQUEST")) {
                    String requester = messageParts[1];
                    messenger.handleIncomingConnectionRequest(requester);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
