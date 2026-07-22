

import java.io.*;
import java.net.*;
import Services.User_auth; //why is it showing

public class Client {
    public static void main(String[] args) {
        try {
            String serverIp = javax.swing.JOptionPane.showInputDialog(
                null, 
                "Enter Server IP Address:", 
                "Connect to Server", 
                javax.swing.JOptionPane.QUESTION_MESSAGE
            );

            if (serverIp == null || serverIp.trim().isEmpty()) {
                serverIp = "localhost";
            }

            Socket s = new Socket(serverIp, 10100);
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            // autoflush means message will be pushed through pipeline immediately without
            // waiting for the buffer to fill up


            //AWS Load balancer and has a time limit, it closes the connection if it is not being utilised for (usually) 60s so we send a ping every 15s to keep the connection alive
            Thread keepAlive = new Thread(()->{
                while(true){
                    try {
                        pw.println("PING");
                        Thread.sleep(15000);
                    } catch (Exception e) {
                        System.out.println("Failed to send ping message");
                    }
                }
            });
            keepAlive.setDaemon(true); // this means if main thread exits, this thread will also exit
            keepAlive.start();

            User_auth userAuth = new User_auth(pw, br);
            userAuth.setVisible(true);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(
                null, 
                "Could not connect to server: " + e.getMessage(), 
                "Connection Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
            System.out.println("Exception occured : " + e);
        }

    }
}
