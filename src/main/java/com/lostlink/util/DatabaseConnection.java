package com.lostlink.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:lostlink.db";

    private DatabaseConnection() {
        // utility class, no instances
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}