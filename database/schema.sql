CREATE DATABASE IF NOT EXISTS emergency_db;
USE emergency_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    blood_group VARCHAR(10),
    emergency_contact VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS emergencies (
    emergency_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    location VARCHAR(255) NOT NULL,
    incident_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ambulance_id INT NULL,
    hospital_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ambulances (
    ambulance_id INT AUTO_INCREMENT PRIMARY KEY,
    driver_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    vehicle_number VARCHAR(30) NOT NULL UNIQUE,
    ambulance_type VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    base_location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS hospitals (
    hospital_id INT AUTO_INCREMENT PRIMARY KEY,
    hospital_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    available_beds INT NOT NULL DEFAULT 0,
    icu_capacity INT NOT NULL DEFAULT 0,
    phone VARCHAR(15),
    er_ready BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS vitals_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    emergency_id INT NOT NULL,
    pulse_rate INT NULL,
    blood_pressure VARCHAR(20),
    spo2 INT NULL,
    temperature_c DECIMAL(4,1) NULL,
    notes TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emergency_id) REFERENCES emergencies(emergency_id) ON DELETE CASCADE
);
