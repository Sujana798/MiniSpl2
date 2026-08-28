package com.lostlink.dao;



import com.lostlink.model.User;
import com.lostlink.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SQLiteUserDAO implements UserDAO {

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, role, created_at FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getString("created_at"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email: " + e.getMessage(), e);
        }
    }
}
