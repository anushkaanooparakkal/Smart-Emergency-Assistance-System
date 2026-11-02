-- Back up emergency_db before running this script. Run it only once, after the original SEAS schema.
USE emergency_db;

ALTER TABLE users
    ADD COLUMN email VARCHAR(150) NULL,
    ADD COLUMN password_hash VARCHAR(255) NULL,
    ADD COLUMN blood_group VARCHAR(10) NULL,
    ADD UNIQUE KEY uq_users_email (email);

ALTER TABLE emergencies
    ADD COLUMN description TEXT NULL,
    ADD COLUMN ambulance_id INT NULL,
    ADD COLUMN hospital_id INT NULL;

ALTER TABLE ambulances
    ADD COLUMN ambulance_type VARCHAR(30) NULL,
    ADD COLUMN base_location VARCHAR(255) NULL,
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

UPDATE ambulances SET status = UPPER(status);

ALTER TABLE hospitals
    ADD COLUMN er_ready BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE vitals_logs
    ADD COLUMN temperature_c DECIMAL(4,1) NULL,
    ADD COLUMN notes TEXT NULL;
