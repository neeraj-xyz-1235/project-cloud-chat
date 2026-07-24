
import java.sql.*;
    
public class Data_handler {
    public static Connection sharedConnection;
    private static String db_url;
    private static String db_username;
    private static String db_password;

    public Data_handler() {
        db_password = System.getenv("DB_PASSWORD"); //retriving stored password....
        if (db_password == null){
            System.out.println("Environment variable DB_PASSWORD not set");
            System.exit(1);
        }
        db_url = System.getenv("DB_URL");  // retriving stored db url.....
        if (db_url == null){
            System.out.println("Environment variable DB_URL not set");
            System.exit(1);
        }
        db_username = System.getenv("DB_USERNAME");  // retriving stored db username.....
        if (db_username == null){
            System.out.println("Environment variable DB_USERNAME not set");
            System.exit(1);
        }

        try {
            sharedConnection = DriverManager.getConnection(db_url, db_username, db_password); // Create db connection
            Statement stmt = sharedConnection.createStatement(); // creating statement object, which is like a cursor...

            System.out.println("Database connection established successfully.");

            String user_stmt = "CREATE TABLE IF NOT EXISTS users (user_id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL)";
            stmt.executeUpdate(user_stmt);

            System.out.println("User table ready...");
            // creating chat table
            String chat_stmt = "CREATE TABLE IF NOT EXISTS chat_rooms(chat_room_id INT PRIMARY KEY AUTO_INCREMENT, user1_id INT, user2_id INT, FOREIGN KEY(user1_id) REFERENCES users(user_id), FOREIGN KEY(user2_id) REFERENCES users(user_id))";
            stmt.executeUpdate(chat_stmt);
            System.out.println("Chat table ready...");

            // Creating a message table
            String message_stmt = "CREATE TABLE IF NOT EXISTS messages(message_id INT PRIMARY KEY AUTO_INCREMENT, chat_room_id INT, sender_id INT, message TEXT NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(chat_room_id) REFERENCES chat_rooms(chat_room_id), FOREIGN KEY(sender_id) REFERENCES users(user_id))";
            stmt.executeUpdate(message_stmt);
            System.out.println("Message table ready...");

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e);
            System.exit(1);
        }
    }

    public static Connection getConnection() {
        try {
            // Check if connection is dead (2 second timeout)
            if (sharedConnection == null || sharedConnection.isClosed() || !sharedConnection.isValid(2)) {
                System.out.println("Database connection is dead or closed, reconnecting...");
                sharedConnection = DriverManager.getConnection(db_url, db_username, db_password);
            }
        } catch (SQLException e) {
            System.out.println("Failed to reconnect to database: " + e);
        }
        return sharedConnection;
    }
}