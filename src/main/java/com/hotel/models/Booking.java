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
    private static int bookingCounter = 1;

    private int bookingId;
    private Customer customer;
    private Room room;
    private Integer numberOfNights;   // Wrapper class — demonstrates autoboxing
    private LocalDate checkInDate;

    /**
     * Creates a Booking with an auto-generated ID and today's check-in date.
     * @param customer       the guest
     * @param room           the booked room
     * @param numberOfNights number of nights (autoboxed to Integer)
     */
    public Booking(Customer customer, Room room, int numberOfNights) {
        this.bookingId = bookingCounter++;
        setCustomer(customer);
        setRoom(room);
        setNumberOfNights(numberOfNights);   // autoboxing int -> Integer happens here
        this.checkInDate = LocalDate.now();
    }

    public static void setBookingCounter(int value) {
        bookingCounter = value;
    }

    public static int getBookingCounter() {
        return bookingCounter;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getBookingId() {
        return bookingId;
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
