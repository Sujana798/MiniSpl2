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

    /**
     * Runs several related DAO calls as a single atomic unit of work on the
     * shared connection, so a multi-table workflow step (e.g. confirming a
     * match and updating both linked reports) either fully succeeds or is
     * fully rolled back - never partially applied.
     */
    public static void runInTransaction(Runnable work) {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            work.run();
            conn.commit();
        } catch (RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Failed to restore autocommit: " + e.getMessage());
            }
        }
    }
}