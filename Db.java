package com.seas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class Db {
    private Db() {}

    static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("SEAS_DB_URL", "jdbc:mysql://localhost:3306/emergency_db");
        String user = System.getenv().getOrDefault("SEAS_DB_USER", "root");
        String password = System.getenv().getOrDefault("SEAS_DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }
}
