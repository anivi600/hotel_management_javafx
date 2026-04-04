package com.hotel.models;

import java.io.Serializable;

/**
 * Standard room — cheapest tier, no surcharge.
 * Demonstrates INHERITANCE and POLYMORPHISM.
 */
public class StandardRoom extends Room {

    private static final long serialVersionUID = 2L;

    public StandardRoom(int roomNumber, double basePrice) {
        super(roomNumber, RoomType.STANDARD, basePrice);
    }

    /**
     * Tariff equals base price with no surcharge.
     * @return basePrice
     */
    @Override
    public double calculateTariff() {
        return getBasePrice();
    }

    @Override
    public String toString() {
        return "StandardRoom{roomNumber=" + getRoomNumber()
                + ", basePrice=" + getBasePrice()
                + ", tariff=" + calculateTariff()
                + ", available=" + isAvailable() + "}";
    }
}
