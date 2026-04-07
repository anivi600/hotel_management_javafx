package com.hotel.services;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.models.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Manages all room and booking operations (in-memory cache + MySQL via DAOs).
 */
public class RoomService {

    private final ArrayList<Room> rooms = new ArrayList<>();
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final HashMap<Integer, Customer> roomOccupancy = new HashMap<>();

    private final RoomDAO roomDAO;
    private final BookingDAO bookingDAO;
    private final CustomerDAO customerDAO;

    public RoomService(RoomDAO roomDAO, BookingDAO bookingDAO, CustomerDAO customerDAO) {
        this.roomDAO = roomDAO;
        this.bookingDAO = bookingDAO;
        this.customerDAO = customerDAO;
        try {
            loadRoomsFromDb();
            if (rooms.isEmpty()) {
                seedRoomsToDb();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rooms from database", e);
        }
    }

    private void loadRoomsFromDb() throws SQLException {
        rooms.clear();
        rooms.addAll(roomDAO.findAll());
    }

    private void seedRoomsToDb() throws SQLException {
        addRoom(new StandardRoom(101, 1500));
        addRoom(new StandardRoom(102, 1500));
        addRoom(new DeluxeRoom(201, 3000));
        addRoom(new DeluxeRoom(202, 3000));
        addRoom(new LuxuryRoom(301, 6000));
        System.out.println("[RoomService] Seeded rooms into DB.");
    }

    public synchronized void addRoom(Room room) throws SQLException {
        for (Room r : rooms) {
            if (r.getRoomNumber() == room.getRoomNumber()) {
                throw new IllegalArgumentException("Room " + room.getRoomNumber() + " already exists.");
            }
        }
        rooms.add(room);
        roomDAO.insert(room);
        Pair.displayInfo("Room added: " + room);
    }

    public synchronized boolean removeRoom(int roomNumber) throws SQLException {
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.getRoomNumber() == roomNumber) {
                if (!r.isAvailable()) {
                    return false;
                }
                it.remove();
                roomDAO.delete(roomNumber);
                return true;
            }
        }
        return false;
    }

    public List<Room> getAllRooms() {
        List<Room> sorted = new ArrayList<>(rooms);
        Collections.sort(sorted, Comparator.comparingInt(Room::getRoomNumber));
        return Collections.unmodifiableList(sorted);
    }

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

    public synchronized Booking bookRoom(Customer customer, int roomNumber, int numberOfNights) throws SQLException {
        Room room = findRoomByNumber(roomNumber);
        if (room == null || !room.isAvailable()) {
            return null;
        }

        room.setAvailable(false);
        roomDAO.updateAvailability(roomNumber, false);

        Pair<Integer, String> bookingRecord = new Pair<>(roomNumber, customer.getName());
        bookingRecord.display();

        System.out.println("Tariff for room " + roomNumber + ": ₹" + room.calculateTariff());

        roomOccupancy.put(roomNumber, customer);

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

        LocalDate today = LocalDate.now();
        try {
            int bookingId = bookingDAO.insertActive(customer.getCustomerId(), roomNumber, numberOfNights, today);
            customerDAO.updateAllocatedRoom(customer.getCustomerId(), roomNumber);
            customer.setAllocatedRoomNumber(roomNumber);
            return new Booking(bookingId, customer, room, numberOfNights, today);
        } catch (SQLException e) {
            room.setAvailable(true);
            roomDAO.updateAvailability(roomNumber, true);
            roomOccupancy.remove(roomNumber);
            throw e;
        }
    }

    public synchronized Customer checkoutRoom(int roomNumber) throws SQLException {
        Room room = findRoomByNumber(roomNumber);
        if (room == null || room.isAvailable()) {
            return null;
        }
        room.setAvailable(true);
        roomDAO.updateAvailability(roomNumber, true);
        Customer guest = roomOccupancy.remove(roomNumber);
        if (guest != null) {
            customers.removeIf(c -> c.getCustomerId() == guest.getCustomerId());
        }
        return guest;
    }

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

    public synchronized void setRooms(List<Room> loadedRooms) {
        rooms.clear();
        rooms.addAll(loadedRooms);
    }

    public synchronized void setCustomers(List<Customer> loadedCustomers) {
        customers.clear();
        customers.addAll(loadedCustomers);
        roomOccupancy.clear();
        for (Customer c : loadedCustomers) {
            if (c.getAllocatedRoomNumber() > 0) {
                roomOccupancy.put(c.getAllocatedRoomNumber(), c);
            }
        }
    }

    public void reloadRoomsFromDatabase() throws SQLException {
        synchronized (this) {
            loadRoomsFromDb();
        }
    }

    /** Updates availability in memory and in the database (e.g. when removing a customer who held a room). */
    public synchronized void setRoomAvailable(int roomNumber, boolean available) throws SQLException {
        Room r = findRoomByNumber(roomNumber);
        if (r != null) {
            r.setAvailable(available);
            roomDAO.updateAvailability(roomNumber, available);
        }
    }

    /**
     * Rebuilds in-memory occupancy from active bookings (e.g. after app restart).
     */
    public synchronized void syncFromActiveBookings(List<Booking> active) {
        roomOccupancy.clear();
        customers.clear();
        for (Booking b : active) {
            roomOccupancy.put(b.getRoom().getRoomNumber(), b.getCustomer());
            customers.add(b.getCustomer());
        }
    }
}
