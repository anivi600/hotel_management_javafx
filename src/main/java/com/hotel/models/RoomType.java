package com.hotel.models;

import java.io.Serializable;

/**
 * Enum representing the type of hotel room with base price per night.
 * Demonstrates ENUM concept with constructor, field, and method.
 */
public enum RoomType {
    STANDARD(2000),
    DELUXE(3500),
    SUITE(5000);

    private final int pricePerNight;

    RoomType(int pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    /**
     * Calculates the total cost for the given number of nights.
     * @param nights number of nights stayed
     * @return total cost as int
     */
    public int calculateCost(int nights) {
        return pricePerNight * nights;
    }

    /**
     * Returns the base price per night for this room type.
     * @return price per night
     */
    public int getPricePerNight() {
        return pricePerNight;
    }

    @Override
    public String toString() {
        switch (this) {
            case STANDARD: return "Standard";
            case DELUXE:   return "Deluxe";
            case SUITE:    return "Suite";
            default:       return name();
        }
    }
}
