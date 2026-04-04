package com.hotel.services;

import com.hotel.models.*;

import java.io.File;
import java.sql.*;

/**
 * Handles SQLite database connectivity and schema initialization.
 */
public class DatabaseService {

    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + "HotelData";
    private static final String DB_URL = "jdbc:sqlite:" + DATA_DIR + File.separator + "hotel.db";

    public DatabaseService() {
        new File(DATA_DIR).mkdirs();
        initializeSchema();
        seedData();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeSchema() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            // Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL)");

            // Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber INTEGER PRIMARY KEY, " +
                    "roomType TEXT NOT NULL, " +
                    "basePrice REAL NOT NULL, " +
                    "available INTEGER NOT NULL)");

            // Customers Table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "customerId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "contactNumber TEXT NOT NULL, " +
                    "allocatedRoomNumber INTEGER)");

            // Bookings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "bookingId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customerId INTEGER, " +
                    "roomNumber INTEGER, " +
                    "numberOfNights INTEGER, " +
                    "checkInDate TEXT)");

            // Housekeeping Tasks Table
            stmt.execute("CREATE TABLE IF NOT EXISTS housekeeping (" +
                    "taskId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "roomNumber INTEGER, " +
                    "description TEXT, " +
                    "status TEXT, " +
                    "assignedTo TEXT)");

            System.out.println("[DatabaseService] Database schema initialized.");
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error initializing schema: " + e.getMessage());
        }
    }

    private void seedData() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed users
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')");
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('reception', 'rec123', 'Receptionist')");
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('cleaner', 'clean123', 'Housekeeping')");
                System.out.println("[DatabaseService] Seeded users.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Error seeding DB: " + e.getMessage());
        }
    }
    
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseService] Auth error: " + e.getMessage());
        }
        return null;
    }

}
