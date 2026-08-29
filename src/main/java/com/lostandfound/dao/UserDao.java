package com.lostandfound.dao;

import com.lostandfound.db.DatabaseConnection;
import com.lostandfound.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (name, email, student_id, password_hash, role, recovery_code_hash) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getStudentId());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getRecoveryCodeHash());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Failed to insert user: " + e.getMessage());
            return false;
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.out.println("Failed to find user: " + e.getMessage());
        }

        return null;
    }

    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Failed to update password: " + e.getMessage());
            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setStudentId(rs.getString("student_id"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setRecoveryCodeHash(rs.getString("recovery_code_hash"));
        return user;
    }
}