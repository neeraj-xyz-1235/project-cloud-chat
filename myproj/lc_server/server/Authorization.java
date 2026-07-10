
import java.sql.*;
import java.io.*;
import at.favre.lib.crypto.bcrypt.BCrypt;
    


public class Authorization {
    PrintWriter spw; // Declared PrintWriter to send messages back to the client
    ServerCryptUtil scu = new ServerCryptUtil(); 

    public Authorization(PrintWriter spw) {
        this.spw = spw;
    }

    public boolean login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = Data_handler.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if(BCrypt.verifyer().verify(password.toCharArray(), storedPassword).verified){
                    // adding clients to hashmap
                    Server.connected_clients.put(username, spw);

                    // Generate a unique key for the user after successful login
                    String userKey = ServerCryptUtil.generateKey();
                    // Store the generated key in the userKeys HashMap
                    ServerCryptUtil.storeUserKey(username, userKey);
                    spw.println("LOGIN::SUCCESS::" + rs.getInt("user_id")+ "::" + userKey); // send the generated key to the client
                    return true;
                    }
                    else {
                    spw.println("LOGIN::INVALID_CREDENTIALS");
                    return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while checking login credentials: " + e.getMessage());
            spw.println("LOGIN::FAILURE");
        }
        return false;
    }

    public void register(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        Connection conn = Data_handler.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
