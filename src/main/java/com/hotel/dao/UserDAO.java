package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {

    private final DatabaseConnection database;

    public UserDAO(DatabaseConnection database) {
        this.database = database;
    }

    public Optional<User> findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    ));
                }
            }
        }
        return Optional.empty();
    }
}
