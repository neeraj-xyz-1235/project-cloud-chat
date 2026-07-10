
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static ConcurrentHashMap<String, PrintWriter> connected_clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Data_handler(); // Initialize the database

        try {

            ServerSocket ss = new ServerSocket(10100);
            System.out.println("Server is listening on port 10100...");
            

            while (true) { // while loop to continuously accept incoming client connections
                Socket s = ss.accept();
                System.out.println("Client connected!");                
                PrintWriter spw = new PrintWriter(s.getOutputStream(), true);
                Client_handler ch = new Client_handler(s, spw);

                ch.start();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e);
        }
    }
}
