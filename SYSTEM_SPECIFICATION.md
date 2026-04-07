# 🏨 Four Square Hotel Manipal — Full System Specification

This document provides a comprehensive technical overview of the Hotel Management System. It is designed to allow an LLM or a new developer to understand the entire codebase, architecture, and workflow in a single read.

---

## 1. Project Overview
A professional-grade JavaFX application for managing hotel operations. It features a modern, bespoke "Terracotta Luxe" UI and handles everything from guest registration to financial reporting.

- **Objective**: Manage rooms, guests, bookings, and billing with role-based security.
- **Architectural Pattern**: **MVC (Model-View-Controller)** + **Service Layer** + **DAO (Data Access Object)**.
- **Visual Theme**: "Terracotta Luxe" (Deep Browns, Espresso, Warm Ivory, and Terracotta Accents).

---

## 2. Technical Stack
- **Language**: Java 17+
- **UI Framework**: JavaFX 17
- **Database**: MySQL (JDBC)
- **Build System**: Maven
- **File Persistence**: Java Serialization + Random Access File (RAF) + Plain Text.

---

## 3. Directory & Folder Structure

```text
OSDL_Project/
├── schema.sql              # Core MySQL database initialization script.
├── run.bat                 # One-click launch script (configures JDK/Maven).
├── pom.xml                 # Maven dependencies (JavaFX, JDBC).
└── src/main/java/com/hotel/
    ├── MainApp.java        # Main Entry Point & Component Bootstrapper.
    ├── controllers/        # FXML Controllers (Header, Legacy Billing/Room tabs).
    ├── dao/                # Data Access Objects (SQL Query Logic).
    ├── models/             # Data Entities (Room, Customer, Booking, Bill, User).
    ├── services/           # Business Logic Layer (RoomService, BillingService).
    ├── database/           # JDBC Connection Manager.
    ├── tabs/               # Modular Java-based UI Tab Components (The "10 Tabs").
    └── util/               # Helper classes (Alerts, UI formatters).
```

---

## 4. Detailed Component Breakdown

### A. The Models (`com.hotel.models`)
| Class | Purpose | Key Data Fields |
| :--- | :--- | :--- |
| `Room` | Abstract base class for rooms. | RoomNo, BasePrice, Availability. |
| `StandardRoom` | Concrete subclass of Room. | Inherits fields, implements `calculateTariff()`. |
| `DeluxeRoom` | Concrete subclass of Room. | Higher tariff multiplier. |
| `LuxuryRoom` | Premium room subclass. | Maximum tariff multiplier. |
| `Customer` | Represents a guest. | ID, Name, Contact, AllocatedRoomNumber. |
| `Booking` | Links Guest + Room. | ID, Nights, CheckInDate, Status. |
| `Bill` | Final invoice data. | BaseTotal, GST (18%), Discount, GrandTotal. |
| `User` | Authentication object. | Username, Password, Role (Admin/Reception/Cleaner). |

### B. The 10 Functional Tabs (`com.hotel.tabs`)
These are the core modules I implemented to modernize the UI:
1.  **DashboardTab**: Statistics overview (Total Rooms, Occupancy Rate).
2.  **RoomMonitorTab**: Live visual map (Red = Occupied, Green = Vacant).
3.  **BookingTab**: Form to assign a guest to a vacant room.
4.  **CheckoutTab**: Automated billing engine + room vacancy reset.
5.  **GuestsTab**: Master list of all registered customers with refresh logic.
6.  **GuestManagementTab**: Registration form for new incoming guests.
7.  **ReportsTab**: Generates text-based Occupancy and Revenue reports.
8.  **SettingsTab**: Live UI theme toggling (Dark Mode) and System Sync.
9.  **RoomTab**: Inventory view of all room types and pricing.
10. **LoginDialog**: Secure Code-driven authentication modal.

### C. Services & Logic (`com.hotel.services`)
- **`RoomService`**: The "Brain" of the app. Manages the active cache of rooms/guests.
- **`FileService`**: High-performance persistence. Uses **Multithreading** to save data to files in the background without freezing the UI.
- **`BillingService`**: Mathematical engine for calculating taxes, nightly rates, and discounts.

---

## 5. System Workflow: A Day in the Hotel
1.  **Start**: `MainApp` launches `DatabaseService`, which applies `schema.sql` and seeds default users (`admin/admin123`).
2.  **Login**: User logs in. `MainController` removes tabs based on role (e.g., Cleaner cannot see `Billing`).
3.  **Registration**: Receptionist enters guest details in `GuestManagementTab`. Saved to `customers` table.
4.  **Booking**: Guest is linked to a room in `BookingTab`. `RoomService` marks room as `Occupied` in DB.
5.  **Live Monitor**: Monitoring system detects the change. `RoomMonitorTab` turns the room **Red**.
6.  **Checkout**: When guest leaves, `CheckoutTab` generates a `Bill`.
    - **Step A**: SQL `INSERT` into `bills` table.
    - **Step B**: `FileService` writes a human-readable `.txt` receipt to `UserHome/HotelData/bills/`.
    - **Step C**: Room is set back to `Available`.
7.  **Audit**: Admin opens `ReportsTab` to view the **Revenue Summary** (runs `SUM` on `bills` table).

---

## 6. Persistence Strategy
The system uses **Hybrid Persistence** for maximum reliability:
- **MySQL**: Persistent storage for all structured data (users, bookings, bills).
- **Binary Serialization**: `rooms.dat` stores current room objects for disaster recovery.
- **RAF (Random Access File)**: `rooms_raf.dat` provides fixed-length records for instant random lookups.
- **Snapshots**: The "Export Data" feature creates human-readable text instances of the full system state.

---

## 7. Configuration & Setup
1.  **Prerequisites**: JDK 17, Maven, and a local MySQL instance.
2.  **DB Config**: Update `DatabaseConnection.java` with your MySQL `root` password.
3.  **Execution**: Run `run.bat` (Windows) or `mvn javafx:run`.
4.  **Data Path**: All exports and local files are stored in `%USERPROFILE%/HotelData`.

---
*Created by Antigravity AI for the Four Square Hotel Project.*
