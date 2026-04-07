package com.hotel.services;

import com.hotel.database.DatabaseConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes MySQL schema from {@code /com/hotel/schema.sql} and seeds default users when empty.
 * JDBC access for the app goes through {@link DatabaseConnection} and DAO classes.
 */
public class DatabaseService {

    private final DatabaseConnection database;

    public DatabaseService(DatabaseConnection database) {
        this.database = database;
        try {
            applySchema();
            seedUsersIfEmpty();
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DatabaseConnection getDatabase() {
        return database;
    }

    private void applySchema() throws SQLException {
        try (InputStream in = DatabaseService.class.getResourceAsStream("/com/hotel/schema.sql")) {
            if (in == null) {
                throw new SQLException("Resource /com/hotel/schema.sql not found.");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String t = line.trim();
                    if (t.startsWith("--") || t.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
            }
            String[] statements = sb.toString().split(";");
            try (Connection conn = database.getConnection();
                 Statement st = conn.createStatement()) {
                for (String raw : statements) {
                    String sql = raw.trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    st.execute(sql);
                }
            }
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Failed to read or apply schema", e);
        }
        System.out.println("[DatabaseService] MySQL schema applied (CREATE IF NOT EXISTS).");
    }

    private void seedUsersIfEmpty() throws SQLException {
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM users")) {
            if (rs.next() && rs.getInt("c") == 0) {
                st.executeUpdate("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')");
                st.executeUpdate("INSERT INTO users (username, password, role) VALUES ('reception', 'rec123', 'Receptionist')");
                st.executeUpdate("INSERT INTO users (username, password, role) VALUES ('cleaner', 'clean123', 'Housekeeping')");
                System.out.println("[DatabaseService] Seeded default users (admin, reception, cleaner).");
            }
        }
    }
}
