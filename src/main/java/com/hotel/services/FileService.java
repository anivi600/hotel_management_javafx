package com.hotel.services;

import com.hotel.models.*;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Handles all persistence operations for the Hotel Management System.
 *
 * Demonstrates:
 * - FILE I/O APPROACH A: Serialization via ObjectOutputStream / ObjectInputStream
 * - FILE I/O APPROACH B: RandomAccessFile with fixed-length records
 * - MULTITHREADING: background Thread for all save operations
 * - SYNCHRONIZATION: synchronized methods to prevent concurrent write corruption
 * - Platform.runLater() for safe UI updates from background threads
 */
public class FileService {

    // ─── File paths ───────────────────────────────────────────────────────────
    private static final String DATA_DIR  = System.getProperty("user.home") + File.separator + "HotelData";
    private static final String ROOMS_DAT = DATA_DIR + File.separator + "rooms.dat";
    private static final String CUSTS_DAT = DATA_DIR + File.separator + "customers.dat";
    private static final String BILLS_DIR = DATA_DIR + File.separator + "bills";
    private static final String RAF_FILE  = DATA_DIR + File.separator + "rooms_raf.dat";

    // Fixed record layout for RandomAccessFile (Approach B)
    // int(4) + char[20](20*2=40) + double(8) + boolean(1) = 53 bytes
    private static final int  ROOM_TYPE_CHARS   = 20;
    private static final int  RAF_RECORD_SIZE   = 4 + (ROOM_TYPE_CHARS * 2) + 8 + 1;  // 53 bytes

    private Consumer<String> statusCallback;

    public FileService() {
        ensureDirectories();
    }

    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }

    private void ensureDirectories() {
        new File(DATA_DIR).mkdirs();
        new File(BILLS_DIR).mkdirs();
    }

    // ─── Update status via Platform.runLater ──────────────────────────────────
    private void updateStatus(String message) {
        if (statusCallback != null) {
            Platform.runLater(() -> statusCallback.accept(message));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // APPROACH A — SERIALIZATION
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Serializes all rooms to rooms.dat on a background thread.
     * Demonstrates MULTITHREADING (Runnable) + SYNCHRONIZATION.
     */
    public void serializeRooms(List<Room> rooms) {
        List<Room> snapshot = new ArrayList<>(rooms);   // defensive copy
        Runnable task = new Runnable() {
            @Override
            public void run() {
                updateStatus("Saving rooms...");
                doSerializeRooms(snapshot);
                updateStatus("Rooms saved successfully — " + java.time.LocalTime.now()
                        .toString().substring(0, 5));
            }
        };
        Thread thread = new Thread(task, "RoomSerializerThread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Synchronized file-write to prevent race conditions.
     */
    private synchronized void doSerializeRooms(List<Room> rooms) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ROOMS_DAT))) {
            oos.writeObject(new ArrayList<>(rooms));
            Pair.displayInfo("Serialized " + rooms.size() + " rooms to " + ROOMS_DAT);
        } catch (IOException e) {
            System.err.println("[FileService] Error serializing rooms: " + e.getMessage());
        }
    }

    /**
     * Deserializes rooms from rooms.dat.
     * Called on application startup on the FX thread (no background needed for reads).
     */
    @SuppressWarnings("unchecked")
    public List<Room> deserializeRooms() {
        File f = new File(ROOMS_DAT);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (ArrayList<Room>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileService] Could not deserialize rooms: " + e.getMessage());
            return null;
        }
    }

    /**
     * Serializes all customers to customers.dat on a background thread.
     */
    public void serializeCustomers(List<Customer> customers) {
        List<Customer> snapshot = new ArrayList<>(customers);
        Thread thread = new Thread(() -> {
            updateStatus("Saving customers...");
            doSerializeCustomers(snapshot);
            updateStatus("Customers saved — " + java.time.LocalTime.now()
                    .toString().substring(0, 5));
        }, "CustomerSerializerThread");
        thread.setDaemon(true);
        thread.start();
    }

    private synchronized void doSerializeCustomers(List<Customer> customers) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(CUSTS_DAT))) {
            oos.writeObject(new ArrayList<>(customers));
            Pair.displayInfo("Serialized " + customers.size() + " customers to " + CUSTS_DAT);
        } catch (IOException e) {
            System.err.println("[FileService] Error serializing customers: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Customer> deserializeCustomers() {
        File f = new File(CUSTS_DAT);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (ArrayList<Customer>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[FileService] Could not deserialize customers: " + e.getMessage());
            return null;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // APPROACH B — RANDOM ACCESS FILE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Appends a room record to rooms_raf.dat using RandomAccessFile.
     * Fixed record: Room Number (int, 4), Room Type (String padded 20 chars),
     * Price per Night (double, 8), Booking Status (boolean, 1).
     * Total: 53 bytes per record.
     */
    public synchronized void saveToRandomAccessFile(Room room) {
        try (RandomAccessFile raf = new RandomAccessFile(RAF_FILE, "rw")) {
            raf.seek(raf.length());   // append at end
            raf.writeInt(room.getRoomNumber());

            // Pad/truncate room type name to exactly ROOM_TYPE_CHARS characters
            String typeName = room.getRoomType().name();
            StringBuilder padded = new StringBuilder(typeName);
            while (padded.length() < ROOM_TYPE_CHARS) padded.append(' ');
            raf.writeChars(padded.substring(0, ROOM_TYPE_CHARS));

            raf.writeDouble(room.getBasePrice());
            raf.writeBoolean(room.isAvailable());

            Pair.displayInfo("RAF write — Room " + room.getRoomNumber());
        } catch (IOException e) {
            System.err.println("[FileService] RAF write error: " + e.getMessage());
        }
    }

    /**
     * Reads a room record from rooms_raf.dat by room number using seek().
     * @param roomNumber the room to search for
     * @return String summary of room or "Not found"
     */
    public synchronized String readFromRandomAccessFile(int roomNumber) {
        File f = new File(RAF_FILE);
        if (!f.exists()) return "RAF file not found.";
        try (RandomAccessFile raf = new RandomAccessFile(RAF_FILE, "r")) {
            long numRecords = raf.length() / RAF_RECORD_SIZE;
            for (long i = 0; i < numRecords; i++) {
                raf.seek(i * RAF_RECORD_SIZE);
                int rNum = raf.readInt();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < ROOM_TYPE_CHARS; j++) {
                    sb.append(raf.readChar());
                }
                double price   = raf.readDouble();
                boolean avail  = raf.readBoolean();
                if (rNum == roomNumber) {
                    return String.format("RAF Record — Room: %d | Type: %s | Price: %.2f | Available: %b",
                            rNum, sb.toString().trim(), price, avail);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileService] RAF read error: " + e.getMessage());
        }
        return "Room " + roomNumber + " not found in RAF file.";
    }

    /**
     * Updates the booking status (available flag) of a room in rooms_raf.dat.
     */
    public synchronized void updateBookingStatusInRAF(int roomNumber, boolean available) {
        File f = new File(RAF_FILE);
        if (!f.exists()) return;
        try (RandomAccessFile raf = new RandomAccessFile(RAF_FILE, "rw")) {
            long numRecords = raf.length() / RAF_RECORD_SIZE;
            for (long i = 0; i < numRecords; i++) {
                raf.seek(i * RAF_RECORD_SIZE);
                int rNum = raf.readInt();
                if (rNum == roomNumber) {
                    // seek to boolean field position: 4 + (20*2) + 8 = 52
                    raf.seek(i * RAF_RECORD_SIZE + 52);
                    raf.writeBoolean(available);
                    Pair.displayInfo("RAF updated Room " + roomNumber + " available=" + available);
                    return;
                }
                // skip the rest of the record we already read int
                raf.seek((i + 1) * RAF_RECORD_SIZE);
            }
        } catch (IOException e) {
            System.err.println("[FileService] RAF update error: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BILL FILE I/O
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Saves a formatted bill as a .txt file, run on a background thread.
     * Calls onComplete (on the FX thread) after the save finishes.
     */
    public void saveBillToFile(Bill bill, Runnable onComplete) {
        Thread thread = new Thread(() -> {
            updateStatus("Saving bill...");
            doSaveBill(bill);
            updateStatus("Bill saved — " + java.time.LocalTime.now().toString().substring(0, 5));
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        }, "BillSaverThread");
        thread.setDaemon(true);
        thread.start();
    }

    /** Convenience overload with no completion callback. */
    public void saveBillToFile(Bill bill) {
        saveBillToFile(bill, null);
    }

    private synchronized void doSaveBill(Bill bill) {
        Booking booking = bill.getBooking();
        String fileName = BILLS_DIR + File.separator
                + "bill_" + booking.getRoom().getRoomNumber()
                + "_" + booking.getCustomer().getName().replace(" ", "_") + ".txt";
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(formatBillText(bill));
            Pair.displayInfo("Bill saved: " + fileName);
        } catch (IOException e) {
            System.err.println("[FileService] Error saving bill: " + e.getMessage());
        }
    }

    /**
     * Formats a bill as a receipt-style text block.
     */
    public static String formatBillText(Bill bill) {
        Booking booking = bill.getBooking();
        Customer customer = booking.getCustomer();
        Room room = booking.getRoom();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      FOUR SQUARE HOTEL MANIPAL         \n");
        sb.append("           BILLING RECEIPT              \n");
        sb.append("========================================\n");
        sb.append(String.format("  Customer  : %-24s%n", customer.getName()));
        sb.append(String.format("  Contact   : %-24s%n", customer.getContactNumber()));
        sb.append(String.format("  Room No   : %-24d%n", room.getRoomNumber()));
        sb.append(String.format("  Room Type : %-24s%n", room.getRoomType().toString()));
        sb.append(String.format("  Check In  : %-24s%n", booking.getCheckInDate().toString()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("  Price/Night: ₹%-23.2f%n", room.calculateTariff()));
        sb.append(String.format("  Nights     : %-24d%n", booking.getNumberOfNights()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("  Base Total : ₹%-23.2f%n", bill.getBaseTotal()));
        sb.append(String.format("  GST (18%%) : ₹%-23.2f%n", bill.getGst()));
        sb.append(String.format("  Discount   : ₹%-23.2f%n", bill.getDiscount()));
        sb.append("========================================\n");
        sb.append(String.format("  GRAND TOTAL: ₹%-23.2f%n", bill.getGrandTotal()));
        sb.append("========================================\n");
        sb.append("  Thank you for staying with us!      \n");
        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Loads all bill .txt files from the bills directory.
     * @return list of bill text contents (one per file)
     */
    public List<String> loadBillHistory() {
        List<String> bills = new ArrayList<>();
        File dir = new File(BILLS_DIR);
        if (!dir.exists()) return bills;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return bills;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (File f : files) {
            try {
                String content = new String(Files.readAllBytes(f.toPath()));
                bills.add("[" + f.getName() + "]\n" + content);
            } catch (IOException e) {
                bills.add("[Error reading " + f.getName() + "]");
            }
        }
        return bills;
    }

    // ─── Seed sample data on first run ──────────────────────────────────────

    /**
     * Seeds 7 rooms of mixed types when no data files exist.
     */
    public static List<Room> createSampleRooms() {
        List<Room> seed = new ArrayList<>();
        // Standard rooms
        seed.add(new StandardRoom(101, 2000.0));
        seed.add(new StandardRoom(104, 1800.0));
        seed.add(new StandardRoom(107, 2200.0));
        // Deluxe rooms
        seed.add(new DeluxeRoom(102, 3500.0));
        seed.add(new DeluxeRoom(105, 4000.0));
        // Luxury / Suite rooms
        seed.add(new LuxuryRoom(103, 5000.0));
        seed.add(new LuxuryRoom(106, 6000.0));
        return seed;
    }

    /**
     * Seeds 4 sample customers — 2 pre-booked, 2 registered but not yet booked.
     * Caller must mark the booked rooms as unavailable in RoomService.
     * @return list of sample customers
     */
    public static List<Customer> createSampleCustomers() {
        List<Customer> seed = new ArrayList<>();
        // Pre-booked customers (rooms 103 and 105 are occupied)
        seed.add(new Customer("Rahul Sharma",    "9876543210", 103));
        seed.add(new Customer("Priya Singh",     "8765432109", 105));
        // Registered but not yet assigned a room
        seed.add(new Customer("Arjun Mehta",     "7654321098", 0));
        seed.add(new Customer("Sneha Kapoor",    "6543210987", 0));
        return seed;
    }

    // ─── Human-readable data export ───────────────────────────────────────────

    /**
     * Exports all rooms and customers to a readable data_export.txt file.
     * This makes the binary .dat contents viewable without a special tool.
     * @param rooms     current room list
     * @param customers current customer list
     */
    public void exportDataToText(List<Room> rooms, List<Customer> customers, List<String> billHistory) {
        String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-").substring(0, 19);
        String exportFile = DATA_DIR + File.separator + "snapshot_" + timestamp + ".txt";
        try (FileWriter fw = new FileWriter(exportFile)) {
            fw.write(buildExportText(rooms, customers, billHistory));
            Pair.displayInfo("Data snapshot exported to: " + exportFile);
            updateStatus("Exported snapshot_" + timestamp + ".txt");
        } catch (IOException e) {
            System.err.println("[FileService] Export error: " + e.getMessage());
        }
    }

    /**
     * Builds the human-readable export text.
     */
    public static String buildExportText(List<Room> rooms, List<Customer> customers, List<String> billHistory) {
        StringBuilder sb = new StringBuilder();
        String now = java.time.LocalDateTime.now().toString().substring(0, 19).replace("T", " ");

        sb.append("========================================================\n");
        sb.append("        FOUR SQUARE HOTEL MANIPAL — SYSTEM SNAPSHOT    \n");
        sb.append("        Generated: ").append(now).append("\n");
        sb.append("========================================================\n\n");

        // ── Rooms ─────────────────────────────────────────────────────────────
        sb.append(String.format("ROOMS  (%d total)%n", rooms.size()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("%-8s %-12s %-16s %-14s %-12s%n",
                "Room No", "Type", "Base Price/Ngt", "Tariff/Ngt", "Status"));
        sb.append("--------------------------------------------------------\n");
        for (Room r : rooms) {
            sb.append(String.format("%-8d %-12s %-16.2f %-14.2f %-12s%n",
                    r.getRoomNumber(),
                    r.getRoomType().toString(),
                    r.getBasePrice(),
                    r.calculateTariff(),
                    r.isAvailable() ? "Available" : "Occupied"));
        }

        sb.append("\n");

        // ── Customers ─────────────────────────────────────────────────────────
        sb.append(String.format("CUSTOMERS  (%d registered)%n", customers.size()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("%-6s %-22s %-14s %-12s%n",
                "ID", "Name", "Contact", "Room"));
        sb.append("--------------------------------------------------------\n");
        for (Customer c : customers) {
            String roomInfo = c.getAllocatedRoomNumber() > 0
                    ? "Room " + c.getAllocatedRoomNumber() : "(unassigned)";
            sb.append(String.format("%-6d %-22s %-14s %-12s%n",
                    c.getCustomerId(), c.getName(), c.getContactNumber(), roomInfo));
        }

        sb.append("\n");

        // ── Bill History ──────────────────────────────────────────────────────
        sb.append(String.format("BILLING HISTORY (%d records found)%n", billHistory.size()));
        sb.append("--------------------------------------------------------\n");
        for (String bill : billHistory) {
             // Just take the headers of bills to keep export concise
             String[] lines = bill.split("\n");
             if (lines.length > 5) {
                 sb.append(lines[0]).append(" | ").append(lines[6]).append("\n"); // Snapshot filename and grand total
             }
        }

        sb.append("\n========================================================\n");
        sb.append("  END OF SNAPSHOT\n");
        sb.append("========================================================\n");

        return sb.toString();
    }

    public String getDataDirectory() {
        return DATA_DIR;
    }

    public String getBillsDirectory() {
        return BILLS_DIR;
    }
}
