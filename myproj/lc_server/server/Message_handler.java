import java.io.*;
import java.sql.*;

public class Message_handler {
    PrintWriter spw;
    String currentUsername;

    public Message_handler(PrintWriter spw, String currentUsername) {
        this.spw = spw;
        this.currentUsername = currentUsername;
    }

    private void sendEncrypted(PrintWriter pw, String payload, String username) {
        if (username != null && ServerCryptUtil.getUserKey(username) != null) {
            String encrypted = ServerCryptUtil.encrypt(payload, username);
            pw.println("ENCRYPTED::" + encrypted);
        } else {
            pw.println(payload);
        }
    }

    public void handleMessage(int chatRoomId, String sender, String content) {
        String sql = "INSERT INTO messages (chat_room_id, sender_id, message) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/appdata");
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int senderId = Integer.parseInt(sender);

            pstmt.setInt(1, chatRoomId);
            pstmt.setInt(2, senderId);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

            // --- REAL-TIME ROUTING ---
            int targetUserId = -1;
            String senderUsername = "";
            String targetUsername = "";

            // Get chat room participants
            try (PreparedStatement roomStmt = conn
                    .prepareStatement("SELECT user1_id, user2_id FROM chat_rooms WHERE chat_room_id = ?")) {
                roomStmt.setInt(1, chatRoomId);
                ResultSet roomRs = roomStmt.executeQuery();
                if (roomRs.next()) {
                    int u1 = roomRs.getInt("user1_id");
                    int u2 = roomRs.getInt("user2_id");
                    targetUserId = (u1 == senderId) ? u2 : u1;
                }
            }

            // Get usernames
            if (targetUserId != -1) {
                try (PreparedStatement userStmt = conn
                        .prepareStatement("SELECT user_id, username FROM users WHERE user_id = ? OR user_id = ?")) {
                    userStmt.setInt(1, senderId);
                    userStmt.setInt(2, targetUserId);
                    ResultSet userRs = userStmt.executeQuery();
                    while (userRs.next()) { 
                        if (userRs.getInt("user_id") == senderId) {
                            senderUsername = userRs.getString("username");
                        } else {
                            targetUsername = userRs.getString("username");
                        }
                    }
                }

                // Send to target if connected
                if (!targetUsername.isEmpty()) {
                    PrintWriter targetPw = Server.connected_clients.get(targetUsername);
                    if (targetPw != null) {
                        sendEncrypted(targetPw, "MESSAGE::" + senderUsername + "::" + content, targetUsername);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error occurred while saving message: " + e.getMessage());
            sendEncrypted(spw, "MESSAGE::FAILURE", currentUsername);
        }
    }
}
