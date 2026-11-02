package com.seas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyService {
    public long registerUser(String name, String email, String password, String phone, String blood, String contact) throws SQLException {
        String sql = "INSERT INTO users(name,email,password_hash,phone,blood_group,emergency_contact) VALUES(?,?,?,?,?,?)";
        try (Connection c = Db.connect(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name.trim()); p.setString(2, email.trim()); p.setString(3, Passwords.hash(password));
            p.setString(4, phone); p.setString(5, blood); p.setString(6, contact); p.executeUpdate();
            return key(p);
        }
    }

    public long reportEmergency(Long userId, String location, String type, String severity, String description) throws SQLException {
        String sql = "INSERT INTO emergencies(user_id,location,incident_type,severity,description) VALUES(?,?,?,?,?)";
        try (Connection c = Db.connect(); PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (userId == null) p.setNull(1, Types.INTEGER); else p.setLong(1, userId);
            p.setString(2, location.trim()); p.setString(3, type.trim()); p.setString(4, severity); p.setString(5, description);
            p.executeUpdate(); return key(p);
        }
    }

    public void addAmbulance(String driver, String phone, String vehicle, String type, String location) throws SQLException {
        try (Connection c = Db.connect(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO ambulances(driver_name,phone,vehicle_number,ambulance_type,base_location) VALUES(?,?,?,?,?)")) {
            p.setString(1, driver.trim()); p.setString(2, phone); p.setString(3, vehicle.trim()); p.setString(4, type); p.setString(5, location); p.executeUpdate();
        }
    }

    public void addHospital(String name, String address, String phone, int beds, int icu) throws SQLException {
        try (Connection c = Db.connect(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO hospitals(hospital_name,address,phone,available_beds,icu_capacity) VALUES(?,?,?,?,?)")) {
            p.setString(1, name.trim()); p.setString(2, address.trim()); p.setString(3, phone); p.setInt(4, beds); p.setInt(5, icu); p.executeUpdate();
        }
    }

    public void addVitals(long emergencyId, Integer pulse, String bp, Integer spo2, Double temp, String notes) throws SQLException {
        try (Connection c = Db.connect(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO vitals_logs(emergency_id,pulse_rate,blood_pressure,spo2,temperature_c,notes) VALUES(?,?,?,?,?,?)")) {
            p.setLong(1, emergencyId); setInteger(p, 2, pulse); p.setString(3, bp); setInteger(p, 4, spo2);
            if (temp == null) p.setNull(5, Types.DECIMAL); else p.setDouble(5, temp);
            p.setString(6, notes); p.executeUpdate();
        }
    }

    public String dispatchAmbulance(long emergencyId, long hospitalId) throws SQLException {
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try {
                int hospitalExists;
                try (PreparedStatement h = c.prepareStatement("SELECT hospital_id FROM hospitals WHERE hospital_id=?")) {
                    h.setLong(1, hospitalId); try (ResultSet rs = h.executeQuery()) { hospitalExists = rs.next() ? 1 : 0; }
                }
                if (hospitalExists == 0) throw new SQLException("Hospital ID not found.");
                long ambulanceId; String vehicle;
                try (PreparedStatement a = c.prepareStatement("SELECT ambulance_id,vehicle_number FROM ambulances WHERE status='AVAILABLE' ORDER BY ambulance_id LIMIT 1 FOR UPDATE");
                     ResultSet rs = a.executeQuery()) {
                    if (!rs.next()) throw new SQLException("No available ambulance.");
                    ambulanceId = rs.getLong(1); vehicle = rs.getString(2);
                }
                try (PreparedStatement e = c.prepareStatement("UPDATE emergencies SET status='ASSIGNED',ambulance_id=?,hospital_id=? WHERE emergency_id=? AND status='PENDING'")) {
                    e.setLong(1, ambulanceId); e.setLong(2, hospitalId); e.setLong(3, emergencyId);
                    if (e.executeUpdate() != 1) throw new SQLException("Emergency not found or no longer pending.");
                }
                try (PreparedStatement a = c.prepareStatement("UPDATE ambulances SET status='BUSY' WHERE ambulance_id=?")) { a.setLong(1, ambulanceId); a.executeUpdate(); }
                c.commit(); return "Ambulance " + vehicle + " assigned to emergency #" + emergencyId + ".";
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public void updateStatus(long emergencyId, String status) throws SQLException {
        try (Connection c = Db.connect()) {
            c.setAutoCommit(false);
            try {
                Long ambulanceId = null;
                try (PreparedStatement q = c.prepareStatement("SELECT ambulance_id FROM emergencies WHERE emergency_id=? FOR UPDATE")) {
                    q.setLong(1, emergencyId); try (ResultSet rs = q.executeQuery()) { if (!rs.next()) throw new SQLException("Emergency ID not found."); long id = rs.getLong(1); if (!rs.wasNull()) ambulanceId = id; }
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE emergencies SET status=? WHERE emergency_id=?")) { p.setString(1, status); p.setLong(2, emergencyId); p.executeUpdate(); }
                if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) if (ambulanceId != null)
                    try (PreparedStatement p = c.prepareStatement("UPDATE ambulances SET status='AVAILABLE' WHERE ambulance_id=?")) { p.setLong(1, ambulanceId); p.executeUpdate(); }
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<Object[]> getEmergencies() throws SQLException {
        return query("SELECT emergency_id,location,incident_type,severity,e.status,COALESCE(a.vehicle_number,''),COALESCE(h.hospital_name,''),created_at FROM emergencies e LEFT JOIN ambulances a ON a.ambulance_id=e.ambulance_id LEFT JOIN hospitals h ON h.hospital_id=e.hospital_id ORDER BY e.emergency_id DESC");
    }
    public List<Object[]> getHospitals() throws SQLException {
        return query("SELECT hospital_id,hospital_name,address,phone,available_beds,icu_capacity,er_ready FROM hospitals ORDER BY hospital_id");
    }
    public List<Object[]> getAmbulances() throws SQLException {
        return query("SELECT ambulance_id,driver_name,phone,vehicle_number,ambulance_type,status,base_location FROM ambulances ORDER BY ambulance_id");
    }
    private List<Object[]> query(String sql) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = Db.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            ResultSetMetaData m = r.getMetaData(); int n = m.getColumnCount();
            while (r.next()) { Object[] row = new Object[n]; for (int i=1;i<=n;i++) row[i-1]=r.getObject(i); rows.add(row); }
        }
        return rows;
    }
    private static long key(PreparedStatement p) throws SQLException { try (ResultSet r=p.getGeneratedKeys()) { if (!r.next()) throw new SQLException("MySQL did not return a new record ID."); return r.getLong(1); } }
    private static void setInteger(PreparedStatement p, int index, Integer value) throws SQLException { if (value==null) p.setNull(index,Types.INTEGER); else p.setInt(index,value); }
}
