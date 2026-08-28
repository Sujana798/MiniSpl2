package com.lostandfound.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                student_id TEXT,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN')),
                security_question TEXT,
                security_answer_hash TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;
        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            System.out.println("Users table created (or already exists).");

        } catch (SQLException e) {
            System.out.println("Failed to create users table!");
            e.printStackTrace();
        }
    }
}