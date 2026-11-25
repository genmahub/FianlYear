CREATE DATABASE IF NOT EXISTS hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_db;

-- Dedicated Secure DB User
DROP USER IF EXISTS 'hotel_user'@'localhost';
CREATE USER 'hotel_user'@'localhost' IDENTIFIED BY 'Hotel@2026Secure!';
GRANT ALL PRIVILEGES ON hotel_db.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;

-- 1. Admins Table
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Your 7 Admins (Encrypted with SHA-256)
INSERT INTO admins (username, password_hash) VALUES
('Grace',    SHA2('admingd@1245W', 256)),
('Kc',       SHA2('adminkc@adminkc@1516X', 256)),
('Michelle', SHA2('admintm@1652S', 256)),
('Inam',     SHA2('adminia@1824K', 256)),
('Rafiq',    SHA2('adminrq@1471N', 256)),
('Michele',  SHA2('adminkm@1733Z', 256)),
('Peloyame', SHA2('adminkp@1368Q', 256))
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

-- 2. Bookings Table (Linked across index.html, guest.html, and AP.java)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id VARCHAR(12) PRIMARY KEY,
    guest_name VARCHAR(120) NOT NULL,
    guest_email VARCHAR(150) NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    room_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50) DEFAULT 'Pay at Hotel',
    extras_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    extras_summary TEXT,
    special_requests TEXT,
    status ENUM('Active', 'Cancelled') NOT NULL DEFAULT 'Active',
    cancellation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cancelled_at DATETIME NULL,
    source VARCHAR(30) DEFAULT 'Website',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Guest Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_name VARCHAR(120) NOT NULL,
    guest_email VARCHAR(150) NOT NULL,
    rating INT NOT NULL DEFAULT 5,
    review_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Notifications & Guest Login Log (Feeds AP.java Live Notifications)
CREATE TABLE IF NOT EXISTS notifications (
    notif_id INT AUTO_INCREMENT PRIMARY KEY,
    category ENUM('Booking', 'Guest Login', 'Cancellation', 'Review', 'Message', 'Security') NOT NULL,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    sender_email VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
