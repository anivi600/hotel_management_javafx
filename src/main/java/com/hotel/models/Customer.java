package com.hotel.models;

import java.io.Serializable;

/**
 * Represents a hotel customer/guest.
 * Demonstrates ENCAPSULATION with validated setters.
 * Implements Serializable for file persistence.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 5L;

    private int customerId;
    private String name;
    private String contactNumber;
    private int allocatedRoomNumber;

    /**
     * Constructs a Customer with auto-generated ID.
     * @param name               guest's full name
     * @param contactNumber      10-digit contact number
     * @param allocatedRoomNumber room number assigned to this customer
     */
    /**
     * New customer before insert — {@code customerId} is set by the database (see {@link #setCustomerId(int)}).
     */
    public Customer(String name, String contactNumber, int allocatedRoomNumber) {
        this.customerId = 0;
        setName(name);
        setContactNumber(contactNumber);
        setAllocatedRoomNumber(allocatedRoomNumber);
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive.");
        }
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name must not be empty.");
        }
        this.name = name.trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        if (contactNumber == null || !contactNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException("Contact number must be exactly 10 digits.");
        }
        this.contactNumber = contactNumber;
    }

    public int getAllocatedRoomNumber() {
        return allocatedRoomNumber;
    }

    public void setAllocatedRoomNumber(int allocatedRoomNumber) {
        this.allocatedRoomNumber = allocatedRoomNumber;
    }

    @Override
    public String toString() {
        return "Customer{id=" + customerId + ", name=" + name
                + ", contact=" + contactNumber
                + ", room=" + allocatedRoomNumber + "}";
    }
}
