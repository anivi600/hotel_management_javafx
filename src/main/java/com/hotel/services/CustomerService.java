package com.hotel.services;

import com.hotel.models.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all customer records.
 * Demonstrates COLLECTIONS (ArrayList), Iterator, sorted access.
 * All mutating operations are synchronized.
 */
public class CustomerService {

    private final ArrayList<Customer> customers = new ArrayList<>();

    private final DatabaseService databaseService;

    public CustomerService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        loadCustomersFromDb();
        if (customers.isEmpty()) {
            seedCustomersToDb();
        }
    }

    private void loadCustomersFromDb() {
        customers.clear();
        String sql = "SELECT * FROM customers";
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("name"),
                        rs.getString("contactNumber"),
                        rs.getInt("allocatedRoomNumber")
                );
                // Customers array will simply use this c, though we might need to set its id manually if we want exact match.
                c.setCustomerId(rs.getInt("customerId"));
                customers.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedCustomersToDb() {
        addCustomer(new Customer("Ashwin Rao", "9876543210", -1));
        addCustomer(new Customer("Sanjana Patil", "9812345678", -1));
        addCustomer(new Customer("David Costa", "9900887766", -1));
        System.out.println("[CustomerService] Seeded customers into DB.");
    }

    /**
     * Adds a new customer to the registry.
     */
    public synchronized void addCustomer(Customer customer) {
        for (Customer c : customers) {
            if (c.getCustomerId() == customer.getCustomerId()) {
                throw new IllegalArgumentException("Customer ID " + customer.getCustomerId() + " already exists.");
            }
        }
        customers.add(customer);
        System.out.println("[CustomerService] Added: " + customer);

        String sql = "INSERT INTO customers (name, contactNumber, allocatedRoomNumber) VALUES (?, ?, ?)";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getContactNumber());
            pstmt.setInt(3, customer.getAllocatedRoomNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
             e.printStackTrace();
        }
    }

    /**
     * Removes a customer by ID using an Iterator.
     * @return true if removed, false if not found
     */
    public synchronized boolean removeCustomer(int customerId) {
        Iterator<Customer> it = customers.iterator();   // Iterator usage
        while (it.hasNext()) {
            Customer c = it.next();
            if (c.getCustomerId() == customerId) {
                it.remove();
                System.out.println("[CustomerService] Removed customer ID: " + customerId);
                
                String sql = "DELETE FROM customers WHERE customerId = ?";
                try (Connection conn = databaseService.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, customerId);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable sorted list of all customers by ID.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> sorted = new ArrayList<>(customers);
        Collections.sort(sorted, (a, b) -> Integer.compare(a.getCustomerId(), b.getCustomerId()));
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Finds a customer by their ID.
     * @return Customer or null if not found
     */
    public Customer findCustomerById(int customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId() == customerId) {
                return c;
            }
        }
        return null;
    }

    /**
     * Finds a customer by their allocated room number.
     */
    public Customer findCustomerByRoom(int roomNumber) {
        for (Customer c : customers) {
            if (c.getAllocatedRoomNumber() == roomNumber) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<Customer> getCustomersList() {
        return customers;
    }

    /**
     * Replaces entire list — used after deserialization.
     */
    public synchronized void setCustomers(List<Customer> loaded) {
        customers.clear();
        customers.addAll(loaded);
        // recalibrate static ID counter to avoid collision
        int maxId = 0;
        for (Customer c : loaded) {
            if (c.getCustomerId() > maxId) {
                maxId = c.getCustomerId();
            }
        }
        Customer.setIdCounter(maxId + 1);
    }
}
