package com.hotel.dao;

import com.hotel.database.DatabaseConnection;
import com.hotel.models.Bill;
import com.hotel.services.FileService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    private final DatabaseConnection database;

    public BillDAO(DatabaseConnection database) {
        this.database = database;
    }

    public void insert(Bill bill, Integer bookingId) throws SQLException {
        var booking = bill.getBooking();
        var customer = booking.getCustomer();
        var room = booking.getRoom();

        String billText = FileService.formatBillText(bill);
        String sql = """
                INSERT INTO bills (booking_id, customer_name, contact_number, room_number, room_type, nights, check_in_date,
                    base_total, gst_amount, discount_amount, grand_total, bill_text)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (bookingId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, bookingId);
            }
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getContactNumber());
            ps.setInt(4, room.getRoomNumber());
            ps.setString(5, room.getRoomType().name());
            ps.setInt(6, booking.getNumberOfNights());
            ps.setDate(7, Date.valueOf(booking.getCheckInDate()));
            ps.setDouble(8, bill.getBaseTotal());
            ps.setDouble(9, bill.getGst());
            ps.setDouble(10, bill.getDiscount());
            ps.setDouble(11, bill.getGrandTotal());
            ps.setString(12, billText);
            ps.executeUpdate();
        }
    }

    /**
     * Each entry is suitable for display in billing history (filename-style prefix + text).
     */
    public List<String> findAllBillTextsNewestFirst() throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT bill_id, created_at, bill_text FROM bills ORDER BY created_at DESC, bill_id DESC";
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String header = "[bill_db_" + rs.getInt("bill_id") + " — " + rs.getTimestamp("created_at") + "]\n";
                out.add(header + rs.getString("bill_text"));
            }
        }
        return out;
    }
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(grand_total) as total FROM bills";
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }

    public double getAverageTransactionValue() throws SQLException {
        String sql = "SELECT AVG(grand_total) as average FROM bills";
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("average");
        }
        return 0;
    }
}
