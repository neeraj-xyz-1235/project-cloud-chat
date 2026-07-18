
import java.sql.*;
    
public class Data_handler {
    public static Connection sharedConnection;

    public Data_handler() {
        String db_password = System.getenv("DB_PASSWORD"); //retriving stored password....
        if (db_password == null){
            System.out.println("Environment variable DB_PASSWORD not set");
            System.exit(1);
        }
        String db_url = System.getenv("DB_URL");  // retriving stored db url.....
        if (db_url == null){
            System.out.println("Environment variable DB_URL not set");
            System.exit(1);
        }
		String db_username = System.getenv("DB_USERNAME");  // retriving stored db username.....
        if (db_username == null){
            System.out.println("Environment variable DB_USERNAME not set");
            System.exit(1);
        }

        try {
            sharedConnection = DriverManager.getConnection(url, admin, password); // Create db connection
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
        }
    }

    public static Connection getConnection() {
        return sharedConnection;
    }
}