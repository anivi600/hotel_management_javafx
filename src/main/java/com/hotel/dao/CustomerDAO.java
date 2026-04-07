package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    private final DatabaseConnection database;

    public CustomerDAO(DatabaseConnection database) {
        this.database = database;
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserts customer and sets {@link Customer#setCustomerId(int)} from generated keys.
     */
    public void insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, contact_number, allocated_room_number) VALUES (?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getContactNumber());
            int alloc = customer.getAllocatedRoomNumber();
            if (alloc <= 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, alloc);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setCustomerId(keys.getInt(1));
                }
            }
        }
    }

    public void delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    public void updateAllocatedRoom(int customerId, int roomNumber) throws SQLException {
        String sql = "UPDATE customers SET allocated_room_number = ? WHERE customer_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (roomNumber <= 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, roomNumber);
            }
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public static Customer mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("customer_id");
        String name = rs.getString("name");
        String contact = rs.getString("contact_number");
        int allocRoom = rs.getObject("allocated_room_number") == null
                ? 0
                : rs.getInt("allocated_room_number");
        Customer c = new Customer(name, contact, allocRoom);
        c.setCustomerId(id);
        return c;
    }
}
