package com.hotel.services;

import com.hotel.models.*;

/**
 * Generates billing details for a checkout booking.
 * Demonstrates use of GENERICS (RoomChargeCalculator<Double>),
 * WRAPPER CLASSES, and billing arithmetic including GST.
 */
public class BillingService {

    private static final double GST_RATE = 0.18;
    private static final double DISCOUNT_PERCENT = 0.05;   // 5% loyalty discount

    /**
     * Generates a Bill for the given booking.
     * Uses RoomChargeCalculator<Double> for the bounded-generic discount calculation.
     * GST is charged at 18% on base total.
     *
     * @param booking         the source booking
     * @param discountPercent custom discount percentage (0.0 – 1.0)
     * @return completed Bill object
     */
    public Bill generateBill(Booking booking, double discountPercent) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }

        Room room = booking.getRoom();

        // Demonstrate POLYMORPHISM: calculateTariff() called on Room reference at runtime
        double pricePerNight = room.calculateTariff();

        // Unboxing Integer -> int for multiplication
        int nights = booking.getNumberOfNights();   // unboxing here

        double baseTotal = pricePerNight * nights;

        double gstAmount = baseTotal * GST_RATE;

        // Apply provided discountPercent; fall back to 5% if 0
        double effectiveDiscountRate = discountPercent > 0 ? discountPercent : DISCOUNT_PERCENT;
        double discountAmount = baseTotal * effectiveDiscountRate;

        // Bounded generic: RoomChargeCalculator<Double> — Double extends Number
        RoomChargeCalculator<Double> calculator =
                new RoomChargeCalculator<>(baseTotal, discountAmount);

        double discountedBase = calculator.getDiscountedTotal();   // unboxing inside
        double grandTotal = discountedBase + gstAmount;

        Pair.displayInfo("Bill generated — baseTotal: " + baseTotal
                + ", GST: " + gstAmount
                + ", discount: " + discountAmount
                + ", grandTotal: " + grandTotal);

        return new Bill(booking, baseTotal, gstAmount, discountAmount, grandTotal);
    }
}
