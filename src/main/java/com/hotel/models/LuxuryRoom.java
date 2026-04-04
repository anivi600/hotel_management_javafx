package com.hotel.models;

import java.io.Serializable;

/**
 * Luxury room — top tier with premium amenities surcharge.
 * Demonstrates INHERITANCE, INTERFACE implementation (Amenities),
 * and POLYMORPHISM.
 */
public class LuxuryRoom extends Room implements Amenities, Serializable {

    private static final long serialVersionUID = 4L;

    public LuxuryRoom(int roomNumber, double basePrice) {
        super(roomNumber, RoomType.SUITE, basePrice);
    }

    /**
     * Tariff = basePrice * 1.5 + 500 (premium amenities flat fee).
     * @return calculated tariff
     */
    @Override
    public double calculateTariff() {
        return getBasePrice() * 1.5 + 500;
    }

    // ─── Amenities interface implementation ──────────────────────────────────

    @Override
    public String provideWifi() {
        return "Luxury Wi-Fi: Dedicated 500 Mbps fiber Wi-Fi with private network.";
    }

    @Override
    public String provideBreakfast() {
        return "Luxury Breakfast: Full gourmet breakfast with à la carte options, 6 AM – 11 AM.";
    }

    @Override
    public String toString() {
        return "LuxuryRoom{roomNumber=" + getRoomNumber()
                + ", basePrice=" + getBasePrice()
                + ", tariff=" + calculateTariff()
                + ", available=" + isAvailable() + "}";
    }
}
