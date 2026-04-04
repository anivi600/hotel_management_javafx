package com.hotel.models;

/**
 * Bounded generic class for calculating discounted room charges.
 * Demonstrates BOUNDED GENERICS concept — T must be a Number.
 * @param <T> a numeric type (Integer, Double, etc.)
 */
public class RoomChargeCalculator<T extends Number> {

    private final T price;
    private final T discount;

    /**
     * Constructs a calculator with a base price and discount amount.
     * @param price    the original base price (numeric)
     * @param discount the discount amount to subtract (numeric)
     */
    public RoomChargeCalculator(T price, T discount) {
        this.price = price;
        this.discount = discount;
    }

    /**
     * Returns the discounted total by subtracting discount from price.
     * Demonstrates UNBOXING of wrapper Number types.
     * @return discounted total as double
     */
    public double getDiscountedTotal() {
        // Unboxing: .doubleValue() extracts primitive from wrapper
        double p = price.doubleValue();
        double d = discount.doubleValue();
        double result = p - d;
        return Math.max(result, 0.0); // ensure non-negative
    }

    public T getPrice() {
        return price;
    }

    public T getDiscount() {
        return discount;
    }

    @Override
    public String toString() {
        return "RoomChargeCalculator{price=" + price + ", discount=" + discount
                + ", discountedTotal=" + getDiscountedTotal() + "}";
    }
}
