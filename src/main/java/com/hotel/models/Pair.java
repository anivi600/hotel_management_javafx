package com.hotel.models;

/**
 * Generic class to associate two related values.
 * Demonstrates GENERICS concept — used to pair room numbers with guest names.
 * @param <T> type of the first element
 * @param <U> type of the second element
 */
public class Pair<T, U> {

    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public U getSecond() {
        return second;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    /**
     * Displays the pair contents in a formatted string.
     */
    public void display() {
        System.out.println("Pair [" + first + " -> " + second + "]");
    }

    /**
     * Generic method to display any type of data.
     * Demonstrates generic method concept.
     * @param data the data to display
     * @param <T> type of data
     */
    public static <T> void displayInfo(T data) {
        System.out.println("[INFO] " + data.toString());
    }

    @Override
    public String toString() {
        return "Pair{first=" + first + ", second=" + second + "}";
    }
}
