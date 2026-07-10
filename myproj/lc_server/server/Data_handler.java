
import java.sql.*;
    
public class Data_handler {
    public static Connection sharedConnection;

    public Data_handler() {
        String url = "jdbc:mysql://localhost:3306/appdata?createDatabaseIfNotExist=true"; // creating MySQL db 

        try {
            sharedConnection = DriverManager.getConnection(url); // Create db connection
            Statement stmt = sharedConnection.createStatement(); // creating statement object, which is like a cursor...

            System.out.println("Database connection established successfully.");

            String user_stmt = "CREATE TABLE IF NOT EXISTS users (user_id  INTEGER PRIMARY KEY AUTO_INCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL)";
            stmt.executeUpdate(user_stmt);

            System.out.println("User table ready...");
            // creating chat table
            String chat_stmt = "CREATE TABLE IF NOT EXISTS chat_rooms(chat_room_id INTEGER PRIMARY KEY AUTO_INCREMENT, user1_id INTEGER, user2_id INTEGER, FOREIGN KEY(user1_id) REFERENCES users(user_id), FOREIGN KEY(user2_id) REFERENCES users(user_id))";
            stmt.executeUpdate(chat_stmt);
            System.out.println("Chat table ready...");

            // Creating a message table
            String message_stmt = "CREATE TABLE IF NOT EXISTS messages(message_id INTEGER PRIMARY KEY AUTO_INCREMENT, chat_room_id INTEGER, sender_id INTEGER, message TEXT NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(chat_room_id) REFERENCES chat_rooms(chat_room_id), FOREIGN KEY(sender_id) REFERENCES users(user_id))";
            stmt.executeUpdate(message_stmt);
            System.out.println("Message table ready...");

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e);
        }
    }

    public static Connection getConnection() {
        return sharedConnection;
    }
}