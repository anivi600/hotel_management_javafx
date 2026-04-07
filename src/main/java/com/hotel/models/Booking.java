package com.hotel.models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a booking record linking a Customer to a Room for a given duration.
 * Demonstrates ENCAPSULATION and use of WRAPPER CLASSES (Integer for numberOfNights).
 * Implements Serializable for persistence.
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 6L;

    private int bookingId;
    private Customer customer;
    private Room room;
    private Integer numberOfNights;   // Wrapper class — demonstrates autoboxing
    private LocalDate checkInDate;

    /**
     * Active booking loaded from DB or created after insert (ID from database).
     */
    public Booking(int bookingId, Customer customer, Room room, int numberOfNights, LocalDate checkInDate) {
        this.bookingId = bookingId;
        setCustomer(customer);
        setRoom(room);
        setNumberOfNights(numberOfNights);
        this.checkInDate = checkInDate;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive.");
        }
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null.");
        }
        this.room = room;
    }

    public Integer getNumberOfNights() {
        return numberOfNights;   // unboxed when needed in arithmetic
    }

    public void setNumberOfNights(Integer numberOfNights) {
        if (numberOfNights == null || numberOfNights <= 0) {
            throw new IllegalArgumentException("Number of nights must be a positive integer.");
        }
        this.numberOfNights = numberOfNights;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    @Override
    public String toString() {
        return "Booking{id=" + bookingId
                + ", customer=" + (customer != null ? customer.getName() : "null")
                + ", room=" + (room != null ? room.getRoomNumber() : "null")
                + ", nights=" + numberOfNights
                + ", checkIn=" + checkInDate + "}";
    }
}
