package com.lostlink.app;


import com.lostlink.util.DatabaseConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class AppBootstrap {

    private AppBootstrap() {
    }

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String schemaSql = readSchemaFile();
            for (String statement : schemaSql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private static String readSchemaFile() throws IOException {
        try (InputStream is = AppBootstrap.class.getResourceAsStream("/db/schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}