
import java.io.*;
import java.sql.*;

public class Connection_handler {
    PrintWriter spw; // Declared PrintWriter to send messages back to the client
    String currentUsername;

    public Connection_handler(PrintWriter spw, String currentUsername) {
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

    public void connectToUser(String targetUser, int currentUserId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";
        Connection conn = Data_handler.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
                    if (targetPw != null) { //if the user doesnt have printwriter, it is assumed that the user is offline
                        sendEncrypted(targetPw, "CONNECT_REQUEST::" + currentUser, targetUser);
                    } else {
                        sendEncrypted(spw, "CONNECT::USER_NOT_FOUND::-1::dummy::dummy", currentUsername); //dummy::dummy is used to avoid array out of bound
                    }
                } else {
                    sendEncrypted(spw, "CONNECT::USER_NOT_FOUND::-1::dummy::dummy", currentUsername);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while checking user existence: " + e.getMessage());
            sendEncrypted(spw, "CONNECT::FAILURE::-1::dummy::dummy", currentUsername);
        }
    }

    public void acceptConnection(String requester) {
        String acceptor = "";
        for (java.util.Map.Entry<String, PrintWriter> entry : Server.connected_clients.entrySet()) { //looping though connected clients
            if (entry.getValue().equals(spw)) {
                acceptor = entry.getKey();
                break;
            }
        }

        int acceptorId = -1;
        int requesterId = -1;

        Connection conn = Data_handler.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT user_id, username FROM users WHERE username = ? OR username = ?")) {
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
                String chatRoomSql = "INSERT IGNORE INTO chat_rooms (user1_id, user2_id) VALUES (?, ?)";
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
                    sendEncrypted(requesterPw, "CONNECT::SUCCESS::" + chatRoomId + "::" + acceptor + "::" + requester, requester);
                }
                sendEncrypted(spw, "CONNECT::SUCCESS::" + chatRoomId + "::" + requester + "::" + acceptor, currentUsername);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred during acceptConnection: " + e.getMessage());
        }
    }

    public void rejectConnection(String requester) {
        PrintWriter requesterPw = Server.connected_clients.get(requester);
        if (requesterPw != null) {
            sendEncrypted(requesterPw, "CONNECT::REJECTED::-1::dummy::dummy", requester);
        }
    }
}
