-- Hotel Management — MySQL schema (matches Java models)
-- Run manually or via app startup (DatabaseService).

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rooms (
    room_number INT PRIMARY KEY,
    room_type VARCHAR(32) NOT NULL,
    base_price DOUBLE NOT NULL,
    available TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    allocated_room_number INT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    room_number INT NOT NULL,
    number_of_nights INT NOT NULL,
    check_in_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_number) REFERENCES rooms (room_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NULL,
    customer_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    room_number INT NOT NULL,
    room_type VARCHAR(32) NOT NULL,
    nights INT NOT NULL,
    check_in_date DATE NOT NULL,
    base_total DOUBLE NOT NULL,
    gst_amount DOUBLE NOT NULL,
    discount_amount DOUBLE NOT NULL,
    grand_total DOUBLE NOT NULL,
    bill_text LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS housekeeping (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number INT NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    assigned_to VARCHAR(64) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
