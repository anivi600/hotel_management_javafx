package com.hotel.models;

import java.io.Serializable;

/**
 * Deluxe room — includes Wi-Fi and breakfast amenities, 20% surcharge.
 * Demonstrates INHERITANCE, INTERFACE implementation (Amenities),
 * and POLYMORPHISM.
 */
public class DeluxeRoom extends Room implements Amenities, Serializable {

    private static final long serialVersionUID = 3L;

    public DeluxeRoom(int roomNumber, double basePrice) {
        super(roomNumber, RoomType.DELUXE, basePrice);
    }

    /**
     * Tariff = basePrice * 1.2 (20% premium surcharge).
     * @return calculated tariff
     */
    @Override
    public double calculateTariff() {
        return getBasePrice() * 1.2;
    }

    // ─── Amenities interface implementation ──────────────────────────────────

    @Override
    public String provideWifi() {
        return "Deluxe Wi-Fi: High-speed 100 Mbps complimentary Wi-Fi included.";
    }

    @Override
    public String provideBreakfast() {
        return "Deluxe Breakfast: Continental breakfast served 7 AM – 10 AM.";
    }

    @Override
    public String toString() {
        return "DeluxeRoom{roomNumber=" + getRoomNumber()
                + ", basePrice=" + getBasePrice()
                + ", tariff=" + calculateTariff()
                + ", available=" + isAvailable() + "}";
    }
}
