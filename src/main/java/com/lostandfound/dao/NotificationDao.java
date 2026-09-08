package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    public boolean insertNotification(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Failed to insert notification: " + e.getMessage());
            return false;
        }
    }

    public List<Notification> findByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch notifications: " + e.getMessage());
        }

        return notifications;
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) AS total FROM notifications WHERE user_id = ? AND is_read = 0";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.out.println("Failed to count unread notifications: " + e.getMessage());
        }

        return 0;
    }

    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Failed to mark notification as read: " + e.getMessage());
            return false;
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}