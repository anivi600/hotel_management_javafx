package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAO {

    private final DatabaseConnection database;

    public RoomDAO(DatabaseConnection database) {
        this.database = database;
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Room> findByRoomNumber(int roomNumber) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, base_price, available) VALUES (?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getBasePrice());
            ps.setInt(4, room.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void delete(int roomNumber) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_number = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            ps.executeUpdate();
        }
    }

    public void updateAvailability(int roomNumber, boolean available) throws SQLException {
        String sql = "UPDATE rooms SET available = ? WHERE room_number = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setInt(2, roomNumber);
            ps.executeUpdate();
        }
    }

    public void updateFull(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_type = ?, base_price = ?, available = ? WHERE room_number = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomType().name());
            ps.setDouble(2, room.getBasePrice());
            ps.setInt(3, room.isAvailable() ? 1 : 0);
            ps.setInt(4, room.getRoomNumber());
            ps.executeUpdate();
        }
    }

    public static Room mapRow(ResultSet rs) throws SQLException {
        int roomNumber = rs.getInt("room_number");
        String roomTypeStr = rs.getString("room_type");
        double basePrice = rs.getDouble("base_price");
        boolean available = rs.getInt("available") == 1;

        if ("LUXURY".equalsIgnoreCase(roomTypeStr)) {
            roomTypeStr = "SUITE";
        }
        RoomType type = RoomType.valueOf(roomTypeStr.toUpperCase());
        Room room;
        switch (type) {
            case STANDARD:
                room = new StandardRoom(roomNumber, basePrice);
                break;
            case DELUXE:
                room = new DeluxeRoom(roomNumber, basePrice);
                break;
            case SUITE:
                room = new LuxuryRoom(roomNumber, basePrice);
                break;
            default:
                room = new StandardRoom(roomNumber, basePrice);
        }
        room.setAvailable(available);
        return room;
    }
}
