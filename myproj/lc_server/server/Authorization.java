
import java.sql.*;
import java.io.*;

public class Authorization {
    PrintWriter spw; // Declared PrintWriter to send messages back to the client

    public Authorization(PrintWriter spw) {
        this.spw = spw;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:users.db");
    }

    public void login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User " + username + " logged in successfully.");
                    spw.println("LOGIN::SUCCESS::" + rs.getInt("user_id"));
                    // adding clients to hashmap
                    Server.connected_clients.put(username, spw);
                } else {
                    spw.println("LOGIN::INVALID_CREDENTIALS");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while checking login credentials: " + e.getMessage());
            spw.println("LOGIN::FAILURE");
        }
    }

    public void register(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            System.out.println("User " + username + " registered successfully.");
            spw.println("REGISTER::SUCCESS");
        } catch (SQLException e) {
            System.out.println("Error occurred while registering a new user: " + e.getMessage());
            spw.println("REGISTER::FAILURE");
        }
    }
}
