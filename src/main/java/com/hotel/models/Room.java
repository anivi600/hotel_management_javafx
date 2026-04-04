package com.hotel.models;

import java.io.Serializable;

/**
 * Abstract base class for all hotel rooms.
 * Demonstrates ENCAPSULATION (private fields + getters/setters),
 * INHERITANCE (subclasses extend this), and ABSTRACTION (abstract method).
 * Implements Serializable for file persistence.
 */
public abstract class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private int roomNumber;
    private RoomType roomType;
    private double basePrice;
    private boolean available;

    /**
     * Constructs a Room with the given parameters.
     * @param roomNumber unique room identifier
     * @param roomType   type of the room (STANDARD, DELUXE, SUITE)
     * @param basePrice  nightly base rate — must be positive
     */
    public Room(int roomNumber, RoomType roomType, double basePrice) {
        setRoomNumber(roomNumber);
        this.roomType = roomType;
        setBasePrice(basePrice);
        this.available = true;
    }

    // ─── Abstract method ──────────────────────────────────────────────────────

    /**
     * Calculates the tariff for this room.
     * Subclasses override this to apply type-specific pricing logic.
     * @return calculated tariff as double
     */
    public abstract double calculateTariff();

    // ─── Concrete method ─────────────────────────────────────────────────────

    /**
     * Displays room details to standard output.
     * Demonstrates POLYMORPHISM when called via Room reference.
     */
    public void displayRoomDetails() {
        System.out.println("===== Room Details =====");
        System.out.println("Room Number : " + roomNumber);
        System.out.println("Type        : " + roomType);
        System.out.println("Base Price  : ₹" + basePrice);
        System.out.println("Tariff      : ₹" + calculateTariff());
        System.out.println("Available   : " + (available ? "Yes" : "No"));
        System.out.println("========================");
    }

    // ─── Getters & Setters (ENCAPSULATION) ───────────────────────────────────

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Room number must be a positive integer.");
        }
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        if (basePrice <= 0) {
            throw new IllegalArgumentException("Base price must be a positive value.");
        }
        this.basePrice = basePrice;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Room{roomNumber=" + roomNumber + ", type=" + roomType
                + ", basePrice=" + basePrice + ", available=" + available + "}";
    }
}
