package com.lostandfound.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

    public class DatabaseConnection {
        private static final String DB_URL = "jdbc:sqlite:lostandfound.db";
        private static Connection connection;
        private DatabaseConnection() {
        }

        public static Connection getConnection() {
            if (connection == null) {
                try {
                    connection = DriverManager.getConnection(DB_URL);
                    System.out.println("Database connected successfully!");
                } catch (SQLException e) {
                    System.out.println("Database connection failed!");
                    e.printStackTrace();
                }
            }
            return connection;
        }
    }

