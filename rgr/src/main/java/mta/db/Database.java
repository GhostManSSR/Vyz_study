package mta.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:postgresql://192.168.128.1:5432/mta";

    private static final String USER = "postgres";
    private static final String PASS = "12345";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void init() throws Exception {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS messages (" +
                            "id SERIAL PRIMARY KEY," +
                            "sender VARCHAR(255) NOT NULL," +
                            "recipient VARCHAR(255) NOT NULL," +
                            "body TEXT," +
                            "queued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "sent BOOLEAN DEFAULT FALSE," +
                            "spam BOOLEAN DEFAULT FALSE," +
                            "spf_pass BOOLEAN DEFAULT FALSE" +
                            ");"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS mail_queue (" +
                            "id SERIAL PRIMARY KEY," +
                            "message_id INT REFERENCES messages(id)," +
                            "retry_count INT DEFAULT 0," +
                            "next_try TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ");"
            );

            System.out.println("Database initialized successfully");
        }
    }
}
