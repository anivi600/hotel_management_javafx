package com.hotel.models;

import java.io.Serializable;

/**
 * Represents the bill generated on customer checkout.
 * Aggregates a Booking with computed financial totals.
 * Implements Serializable for persistence.
 */
public class Bill implements Serializable {

    private static final long serialVersionUID = 7L;

    private Booking booking;
    private double baseTotal;
    private double gst;
    private double discount;
    private double grandTotal;

    /**
     * Constructs a Bill with all financial details.
     * @param booking   the associated booking
     * @param baseTotal price x nights
     * @param gst       18% GST amount
     * @param discount  discount amount from RoomChargeCalculator
     * @param grandTotal final amount = baseTotal + gst - discount
     */
    public Bill(Booking booking, double baseTotal, double gst, double discount, double grandTotal) {
        setBooking(booking);
        setBaseTotal(baseTotal);
        setGst(gst);
        setDiscount(discount);
        setGrandTotal(grandTotal);
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        this.booking = booking;
    }

    public double getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(double baseTotal) {
        if (baseTotal < 0) {
            throw new IllegalArgumentException("Base total cannot be negative.");
        }
        this.baseTotal = baseTotal;
    }

    public double getGst() {
        return gst;
    }

    public void setGst(double gst) {
        if (gst < 0) {
            throw new IllegalArgumentException("GST cannot be negative.");
        }
        this.gst = gst;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative.");
        }
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        if (grandTotal < 0) {
            throw new IllegalArgumentException("Grand total cannot be negative.");
        }
        this.grandTotal = grandTotal;
    }

    @Override
    public String toString() {
        return "Bill{bookingId=" + (booking != null ? booking.getBookingId() : "null")
                + ", baseTotal=" + baseTotal
                + ", gst=" + gst
                + ", discount=" + discount
                + ", grandTotal=" + grandTotal + "}";
    }
}
