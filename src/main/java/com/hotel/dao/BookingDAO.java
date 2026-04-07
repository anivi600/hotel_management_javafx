package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Booking;
import com.hotel.models.Customer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private final DatabaseConnection database;

    public BookingDAO(DatabaseConnection database) {
        this.database = database;
    }

    public int insertActive(int customerId, int roomNumber, int numberOfNights, LocalDate checkInDate) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, room_number, number_of_nights, check_in_date, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, roomNumber);
            ps.setInt(3, numberOfNights);
            ps.setDate(4, Date.valueOf(checkInDate));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insert booking failed: no generated key.");
    }

    public void delete(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    public List<Booking> findAllActive() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = """
                SELECT b.booking_id, b.customer_id, b.number_of_nights, b.check_in_date,
                       c.name, c.contact_number, c.allocated_room_number,
                       r.room_number AS room_number, r.room_type AS room_type, r.base_price AS base_price, r.available AS available
                FROM bookings b
                JOIN customers c ON c.customer_id = b.customer_id
                JOIN rooms r ON r.room_number = b.room_number
                WHERE b.status = 'ACTIVE'
                ORDER BY b.booking_id
                """;
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getString("name"),
                        rs.getString("contact_number"),
                        rs.getObject("allocated_room_number") == null ? 0 : rs.getInt("allocated_room_number")
                );
                customer.setCustomerId(rs.getInt("customer_id"));

                com.hotel.models.Room room = RoomDAO.mapRow(rs);
                LocalDate checkIn = rs.getDate("check_in_date").toLocalDate();
                int bookingId = rs.getInt("booking_id");
                int nights = rs.getInt("number_of_nights");

                list.add(new Booking(bookingId, customer, room, nights, checkIn));
            }
        }
        return list;
    }
}
