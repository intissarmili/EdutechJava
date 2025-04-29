package service.user;

import models.UserLog;
import utils.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserLogService {

    private final Connection connection;

    public UserLogService() {
        connection = MaConnexion.getInstance().getConnection();
    }

    public void addLog(int userId, String action) {
        String sql = "INSERT INTO user_logs (user_id, action) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout log: " + e.getMessage());
        }
    }

    public List<UserLog> getAllLogs() {
        List<UserLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM user_logs ORDER BY timestamp DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserLog log = new UserLog();
                log.setId(rs.getInt("id"));
                log.setUserId(rs.getInt("user_id"));
                log.setAction(rs.getString("action"));
                log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllLogs: " + e.getMessage());
        }
        return logs;
    }

    // 🆕 NEW METHOD TO GET today's login count
    public int countLoginsToday() {
        String sql = "SELECT COUNT(*) FROM user_logs WHERE action = 'login' AND DATE(timestamp) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countLoginsToday: " + e.getMessage());
        }
        return 0;
    }

}
