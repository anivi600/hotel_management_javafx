package com.hotel.services;

import com.hotel.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Manages all room and booking operations.
 * Demonstrates:
 * - COLLECTIONS: ArrayList<Room>, HashMap<Integer, Customer>
 * - POLYMORPHISM: uses Room base references for calculateTariff()
 * - SYNCHRONIZATION: all write operations are synchronized
 * - GENERICS: Pair<Integer, String> for booking records
 * - WRAPPER CLASSES / AUTOBOXING: Integer room numbers in HashMap
 * - Iterator usage for traversal
 * - Collections.sort() with Comparator
 */
public class RoomService {

    private final ArrayList<Room> rooms = new ArrayList<>();
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final HashMap<Integer, Customer> roomOccupancy = new HashMap<>();  // roomNumber -> Customer

    private final DatabaseService databaseService;

    public RoomService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        loadRoomsFromDb();
        if (rooms.isEmpty()) {
            seedRoomsToDb();
        }
    }

    private void loadRoomsFromDb() {
        rooms.clear();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = databaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int roomNumber = rs.getInt("roomNumber");
                String roomTypeStr = rs.getString("roomType");
                double basePrice = rs.getDouble("basePrice");
                boolean available = rs.getInt("available") == 1;

                Room room;
                if ("STANDARD".equalsIgnoreCase(roomTypeStr)) {
                    room = new StandardRoom(roomNumber, basePrice);
                } else if ("DELUXE".equalsIgnoreCase(roomTypeStr)) {
                    room = new DeluxeRoom(roomNumber, basePrice);
                } else {
                    room = new LuxuryRoom(roomNumber, basePrice);
                }
                room.setAvailable(available);
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedRoomsToDb() {
        addRoom(new StandardRoom(101, 1500));
        addRoom(new StandardRoom(102, 1500));
        addRoom(new DeluxeRoom(201, 3000));
        addRoom(new DeluxeRoom(202, 3000));
        addRoom(new LuxuryRoom(301, 6000));
        System.out.println("[RoomService] Seeded rooms into DB.");
    }

    // ─── Room Management ─────────────────────────────────────────────────────

    /**
     * Adds a room to the inventory.
     * Synchronized to prevent concurrency issues.
     */
    public synchronized void addRoom(Room room) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == room.getRoomNumber()) {
                throw new IllegalArgumentException("Room " + room.getRoomNumber() + " already exists.");
            }
        }
        rooms.add(room);          // autoboxing happens for Integer keys in HashMap
        
        String sql = "INSERT INTO rooms (roomNumber, roomType, basePrice, available) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType().name());
            pstmt.setDouble(3, room.getBasePrice());
            pstmt.setInt(4, room.isAvailable() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
             e.printStackTrace();
        }

        Pair.displayInfo("Room added: " + room);  // generic method demo
    }

    /**
     * Removes a room — only if it is currently available (not booked).
     */
    public synchronized boolean removeRoom(int roomNumber) {
        Iterator<Room> it = rooms.iterator();       // Iterator usage
        while (it.hasNext()) {
            Room r = it.next();
            if (r.getRoomNumber() == roomNumber) {
                if (!r.isAvailable()) {
                    return false;   // blocked — room is booked
                }
                it.remove();
                
                String sql = "DELETE FROM rooms WHERE roomNumber = ?";
                try (Connection conn = databaseService.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, roomNumber);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all rooms, sorted by room number using Collections.sort() + Comparator.
     */
    public List<Room> getAllRooms() {
        List<Room> sorted = new ArrayList<>(rooms);
        // COLLECTIONS.SORT with Comparator — demonstrates sorting
        Collections.sort(sorted, Comparator.comparingInt(Room::getRoomNumber));
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Returns only rooms whose available flag is true.
     */
    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room r : rooms) {
            if (r.isAvailable()) {
                available.add(r);
            }
        }
        Collections.sort(available, Comparator.comparingInt(Room::getRoomNumber));
        return Collections.unmodifiableList(available);
    }

    /**
     * Returns rooms sorted by price (ascending) — demonstrates alternate Comparator.
     */
    public List<Room> getRoomsSortedByPrice() {
        List<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(sorted, Comparator.comparingDouble(Room::getBasePrice));
        return Collections.unmodifiableList(sorted);
    }

    public Room findRoomByNumber(int roomNumber) {
        for (Room r : rooms) {
            if (r.getRoomNumber() == roomNumber) {
                return r;
            }
        }
        return null;
    }

    // ─── Booking & Checkout ───────────────────────────────────────────────────

    /**
     * Books a room for a customer.
     * Demonstrates runtime POLYMORPHISM: calculateTariff() on Room reference.
     * @return Booking object if successful, null if room unavailable
     */
    public synchronized Booking bookRoom(Customer customer, int roomNumber, int numberOfNights) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null || !room.isAvailable()) {
            return null;
        }
        room.setAvailable(false);
        updateRoomAvailabilityInDb(roomNumber, false);

        // Demonstrate Pair<Integer, String> linking room number to guest name
        Pair<Integer, String> bookingRecord = new Pair<>(roomNumber, customer.getName());
        bookingRecord.display();

        // Runtime polymorphism: calculateTariff() resolved at runtime
        System.out.println("Tariff for room " + roomNumber + ": ₹" + room.calculateTariff());

        // Wrapper / Autoboxing: Integer roomNumber is autoboxed when used as HashMap key
        roomOccupancy.put(roomNumber, customer);   // autoboxing int -> Integer

        // Add customer to list if not already present
        boolean exists = false;
        for (Customer c : customers) {
            if (c.getCustomerId() == customer.getCustomerId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            customers.add(customer);
        }

        return new Booking(customer, room, numberOfNights);
    }

    /**
     * Checks out a customer from a room — releases the room.
     * @return the Customer who was checked out, or null
     */
    public synchronized Customer checkoutRoom(int roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null || room.isAvailable()) {
            return null;
        }
        room.setAvailable(true);
        updateRoomAvailabilityInDb(roomNumber, true);
        // Unboxing: Integer key unboxed to int for removal logic
        Customer guest = roomOccupancy.remove(roomNumber);  // Integer key unboxed here
        if (guest != null) {
            customers.removeIf(c -> c.getCustomerId() == guest.getCustomerId());
        }
        return guest;
    }

    private void updateRoomAvailabilityInDb(int roomNumber, boolean available) {
        String sql = "UPDATE rooms SET available = ? WHERE roomNumber = ?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setInt(2, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── Direct collection access for serialization ───────────────────────────

    public ArrayList<Room> getRoomsList() {
        return rooms;
    }

    public ArrayList<Customer> getCustomersList() {
        return customers;
    }

    public HashMap<Integer, Customer> getRoomOccupancy() {
        return roomOccupancy;
    }

    public boolean isRoomOccupied(int roomNumber) {
        return roomOccupancy.containsKey(roomNumber);
    }

    /**
     * Restores deserialized rooms into this service's list.
     */
    public synchronized void setRooms(List<Room> loadedRooms) {
        rooms.clear();
        rooms.addAll(loadedRooms);
    }

    /**
     * Restores deserialized customers into this service's list.
     */
    public synchronized void setCustomers(List<Customer> loadedCustomers) {
        customers.clear();
        customers.addAll(loadedCustomers);
        // rebuild occupancy map
        roomOccupancy.clear();
        for (Customer c : loadedCustomers) {
            if (c.getAllocatedRoomNumber() > 0) {
                roomOccupancy.put(c.getAllocatedRoomNumber(), c);  // autoboxing
            }
        }
    }
}
