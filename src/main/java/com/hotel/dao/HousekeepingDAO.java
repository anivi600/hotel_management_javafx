package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.HousekeepingTask;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HousekeepingDAO {

    private final DatabaseConnection database;

    public HousekeepingDAO(DatabaseConnection database) {
        this.database = database;
    }

    public List<HousekeepingTask> findAllOrderByStatus() throws SQLException {
        List<HousekeepingTask> list = new ArrayList<>();
        String sql = """
                SELECT * FROM housekeeping
                ORDER BY CASE status
                    WHEN 'Pending' THEN 0
                    WHEN 'In Progress' THEN 1
                    ELSE 2
                END, task_id
                """;
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(int roomNumber, String description, String status, String assignedTo) throws SQLException {
        String sql = "INSERT INTO housekeeping (room_number, description, status, assigned_to) VALUES (?, ?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            ps.setString(2, description);
            ps.setString(3, status);
            if (assignedTo == null || assignedTo.isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, assignedTo);
            }
            ps.executeUpdate();
        }
    }

    public void updateStatus(int taskId, String status) throws SQLException {
        String sql = "UPDATE housekeeping SET status = ? WHERE task_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    public void delete(int taskId) throws SQLException {
        String sql = "DELETE FROM housekeeping WHERE task_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    public static HousekeepingTask mapRow(ResultSet rs) throws SQLException {
        return new HousekeepingTask(
                rs.getInt("task_id"),
                rs.getInt("room_number"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("assigned_to")
        );
    }
}
