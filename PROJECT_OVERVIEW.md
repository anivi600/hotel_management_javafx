# Four Square Hotel Manipal - Project Overview

This document outlines the workflow, comprehensive feature list, and directory structure of the Four Square Hotel Manipal Management System.

## 🚀 Project Workflow

The application runs as a local desktop JavaFX client integrated with a local SQLite database for persistence.

1. **Application Launch (`MainApp.java`)**: The system boots up and connects to `hotel.db` through `DatabaseService`. If the database is missing, it automatically creates the necessary tables (`users`, `rooms`, `customers`, `bookings`, `housekeeping`) and seeds default users.
2. **Authentication (`LoginController.java`)**: Users must authenticate. The system checks their credentials against the `users` table and retrieves their assigned role.
3. **Role-Based Routing (`MainController.java`)**: Upon successful login, the user is redirected to the main dashboard. UI Tabs are dynamically loaded and filtered based on the logged-in user's role:
    - **Admin**: Has full access to all tabs (Rooms, Customers, Bookings, Billing, Housekeeping).
    - **Receptionist**: Has access only to operational tabs (Rooms, Customers, Bookings, Billing).
    - **Housekeeping**: Has exclusive access only to the Housekeeping tab.
4. **Operations (`*Service.java` & DB)**: The UI communicates with specific `Service` classes (e.g., `RoomService`, `CustomerService`), which process the logic and dispatch SQL queries via JDBC to persist data modifications in real-time.

---

## ✨ Features Present in the Project

### 1. Security & Authentication
*   **Secure User Login**: Enforces a gateway screen to restrict unauthorized usage.
*   **Role-Based Access Control (RBAC)**: Segregates duties by enabling/disabling system modules based on employee designation (Admin, Receptionist, Cleaner).

### 2. Room Management
*   **Inventory Control**: Add or remove available rooms from the application.
*   **Polymorphic Tiers**: Supports various room classifications (`StandardRoom`, `DeluxeRoom`, `LuxuryRoom`) which calculate pricing and tariffs uniquely.
*   **Availability Tracking**: Filters and tracks whether a room is vacant or currently occupied.

### 3. Customer Management
*   **Guest Registry**: Records and organizes hotel guests along with their contact information.
*   **Active Directory**: Quickly search for guests based on their randomly generated unique identification numbers. 

### 4. Booking & Checkout System
*   **Reservations**: Allocate rooms to active customers for a designated number of nights.
*   **Tariff Calculation**: Dynamically computes pricing based on the assigned room tier and duration of stay.
*   **Checkout & Release**: Releases the room back into the pool while finalizing the stay for the guest.

### 5. Housekeeping Management
*   **Task Dispatch**: Allows the creation of cleaning or maintenance tasks mapped to specific rooms.
*   **Live Status Updates**: Staff can log in and update the status of existing tasks (`Pending`, `In Progress`, `Completed`).
*   **Task Assignment**: Allows staff assignment tracking. 

### 6. Billing & Invoicing (Persistence)
*   **File Generation**: Auto-generates billing history using `FileService` for flat-file/text representation of invoices upon checkout.

### 7. Core Technology Capabilities
*   **SQLite Database Integration**: Robust local data storage powered by `JDBC` with `java.sql.*` queries.
*   **Modern UI (Glassmorphism)**: Uses cutting-edge JavaFX CSS (`styles.css`) for sleek, premium graphical interfaces resembling top-tier applications.
*   **Multithreading**: Synchronized collections ensure data mutations do not crash the state concurrently.

---

## 📂 Where Can I Find What? (Code Map)

### Main Application
*   `src/main/java/com/hotel/MainApp.java`: The core entry point, starts the GUI, and handles dynamic View/Controller injections based on authentication state.

### The Models (Data Types)
*(Located in `src/main/java/com/hotel/models/`)*
*   `User.java`: Authentication details and Role assignments.
*   `Room.java` / `StandardRoom.java` / `DeluxeRoom.java` / `LuxuryRoom.java`: Handles the different tiers of room capacities and prices.
*   `Customer.java`: Details regarding human guests.
*   `Booking.java` / `Pair.java`: Binds a Customer to a Room for a duration.
*   `HousekeepingTask.java`: Defines cleaning assignments.

### The Services (Backend Logic & Database)
*(Located in `src/main/java/com/hotel/services/`)*
*   `DatabaseService.java`: The central brain for SQLite creation, connection pooling, and credential verification.
*   `CustomerService.java`: SQL logic for adding and deleting `customers`.
*   `RoomService.java`: SQL logic for adding, deleting, and fetching `rooms` and updating availability.
*   `FileService.java`: Used primarily for legacy dat-file parsing and bill/invoice generation logic.
*   `BillingService.java`: Math and calculations for when guests checkout.

### The Controllers (Dashboard Logic)
*(Located in `src/main/java/com/hotel/controllers/`)*
*   `LoginController.java`: Intercepts the username/password window.
*   `MainController.java`: Contains the RBAC logic to hide/show system Tabs.
*   `RoomController.java` / `CustomerController.java` / `BookingController.java` / `BillingController.java` / `HousekeepingController.java`: Directly interfaces buttons from the application to trigger changes in the Backend Services.

### The User Interface Views (Frontend)
*(Located in `src/main/resources/com/hotel/`)*
*   `login_view.fxml`: The beautiful, gradient entry window.
*   `main_view.fxml`: The tabbed, horizontal layout foundation.
*   `housekeeping_tab.fxml`, `room_tab.fxml`, `customer_tab.fxml`, `booking_tab.fxml`, `billing_tab.fxml`: Individual layout descriptions for their respective panels.
*   `styles.css`: The "Ocean Gold" UI toolkit styling all backgrounds, buttons, fonts, and borders.
