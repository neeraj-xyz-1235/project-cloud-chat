
import java.io.*;
import java.sql.*;

public class Connection_handler {
    PrintWriter spw; // Declared PrintWriter to send messages back to the client

    public Connection_handler(PrintWriter spw) {
        this.spw = spw;
    }

    public void connectToUser(String targetUser, int currentUserId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String currentUser = "";
            pstmt.setInt(1, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentUser = rs.getString("username");
                }
            }

            String targetSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement targetPstmt = conn.prepareStatement(targetSql)) {
                targetPstmt.setString(1, targetUser);
                ResultSet targetRs = targetPstmt.executeQuery();
                if (targetRs.next()) {
                    PrintWriter targetPw = Server.connected_clients.get(targetUser);
                    if (targetPw != null) {
                        targetPw.println("CONNECT_REQUEST::" + currentUser);
                    } else {
                        spw.println("CONNECT::USER_NOT_FOUND::-1::dummy::dummy");
                    }
                } else {
                    spw.println("CONNECT::USER_NOT_FOUND::-1::dummy::dummy");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while checking user existence: " + e.getMessage());
            spw.println("CONNECT::FAILURE::-1::dummy::dummy");
        }
    }

    public void acceptConnection(String requester) {
        String acceptor = "";
        for (java.util.Map.Entry<String, PrintWriter> entry : Server.connected_clients.entrySet()) {
            if (entry.getValue().equals(spw)) {
                acceptor = entry.getKey();
                break;
            }
        }

        int acceptorId = -1;
        int requesterId = -1;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db");
                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT user_id, username FROM users WHERE username = ? OR username = ?")) {
            pstmt.setString(1, acceptor);
            pstmt.setString(2, requester);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("username").equals(acceptor))
                    acceptorId = rs.getInt("user_id");
                if (rs.getString("username").equals(requester))
                    requesterId = rs.getInt("user_id");
            }

            if (acceptorId != -1 && requesterId != -1) {
                String chatRoomSql = "INSERT OR IGNORE INTO chat_rooms (user1_id, user2_id) VALUES (?, ?)";
                try (PreparedStatement chatRoomPstmt = conn.prepareStatement(chatRoomSql)) {
                    chatRoomPstmt.setInt(1, requesterId);
                    chatRoomPstmt.setInt(2, acceptorId);
                    chatRoomPstmt.executeUpdate();
                }

                int chatRoomId = -1;
                try (PreparedStatement chatRoomPstmt = conn.prepareStatement(
                        "SELECT chat_room_id FROM chat_rooms WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)")) {
                    chatRoomPstmt.setInt(1, requesterId);
                    chatRoomPstmt.setInt(2, acceptorId);
                    chatRoomPstmt.setInt(3, acceptorId);
                    chatRoomPstmt.setInt(4, requesterId);
                    ResultSet chatRoomRs = chatRoomPstmt.executeQuery();
                    if (chatRoomRs.next()) {
                        chatRoomId = chatRoomRs.getInt("chat_room_id");
                    }
                }

                PrintWriter requesterPw = Server.connected_clients.get(requester);
                if (requesterPw != null) {
                    requesterPw.println("CONNECT::SUCCESS::" + chatRoomId + "::" + acceptor + "::" + requester);
                }
                spw.println("CONNECT::SUCCESS::" + chatRoomId + "::" + requester + "::" + acceptor);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred during acceptConnection: " + e.getMessage());
        }
    }

    public void rejectConnection(String requester) {
        PrintWriter requesterPw = Server.connected_clients.get(requester);
        if (requesterPw != null) {
            requesterPw.println("CONNECT::REJECTED::-1::dummy::dummy");
        }
    }
}
