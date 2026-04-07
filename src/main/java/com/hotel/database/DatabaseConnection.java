package com.hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC access for MySQL. Adjust credentials via environment variables if needed:
 * HOTEL_DB_URL, HOTEL_DB_USER, HOTEL_DB_PASSWORD
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/hotel_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root1234";

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection() {
        this.url = System.getenv().getOrDefault("HOTEL_DB_URL", DEFAULT_URL);
        this.username = System.getenv().getOrDefault("HOTEL_DB_USER", DEFAULT_USER);
        this.password = System.getenv().getOrDefault("HOTEL_DB_PASSWORD", DEFAULT_PASSWORD);
    }

    public DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public String getUrl() {
        return url;
    }
}
