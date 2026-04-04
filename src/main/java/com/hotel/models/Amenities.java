package com.hotel.models;

/**
 * Interface defining premium amenities available in higher-class rooms.
 * Demonstrates INTERFACE concept.
 */
public interface Amenities {
    /**
     * Provides Wi-Fi information for the room.
     * @return Wi-Fi details string
     */
    String provideWifi();

    /**
     * Provides breakfast information for the room.
     * @return Breakfast details string
     */
    String provideBreakfast();
}
