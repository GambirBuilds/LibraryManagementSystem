package com.library.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages database connections. Credentials / path loaded from config.properties
 * (never hard-coded in source).
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";
    private static String dbUrl;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] SQLite JDBC driver not found on classpath.");
        }
        loadConfig();
        initializeDatabase();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        Path configPath = Paths.get(CONFIG_FILE);

        // Try current directory first, then resources
        try (InputStream is = Files.exists(configPath)
                ? Files.newInputStream(configPath)
                : DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {

            if (is != null) {
                props.load(is);
                dbUrl = props.getProperty("db.url", "jdbc:sqlite:library.db");
            } else {
                // Fallback for first-time run
                dbUrl = "jdbc:sqlite:library.db";
                System.out.println("[INFO] config.properties not found - using default SQLite database: library.db");
            }
        } catch (IOException e) {
            dbUrl = "jdbc:sqlite:library.db";
            System.err.println("[WARN] Could not load config.properties: " + e.getMessage());
        }
    }

    /**
     * Creates the schema if the tables do not yet exist.
     */
    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Load and execute schema.sql from resources
            try (InputStream schemaStream = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("schema.sql")) {

                if (schemaStream != null) {
                    String schema = new String(schemaStream.readAllBytes());
                    // Split by semicolon and execute each statement
                    for (String sql : schema.split(";")) {
                        String trimmed = sql.trim();
                        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                            stmt.execute(trimmed);
                        }
                    }
                } else {
                    // Inline fallback schema
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS books (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            title TEXT NOT NULL,
                            author TEXT NOT NULL,
                            isbn TEXT UNIQUE NOT NULL,
                            category TEXT NOT NULL,
                            total_copies INTEGER NOT NULL,
                            available_copies INTEGER NOT NULL
                        )
                        """);
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS members (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            email TEXT UNIQUE NOT NULL,
                            phone TEXT,
                            join_date TEXT NOT NULL
                        )
                        """);
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS borrowings (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            book_id INTEGER NOT NULL,
                            member_id INTEGER NOT NULL,
                            issue_date TEXT NOT NULL,
                            due_date TEXT NOT NULL,
                            return_date TEXT,
                            status TEXT NOT NULL DEFAULT 'BORROWED',
                            FOREIGN KEY (book_id) REFERENCES books(id),
                            FOREIGN KEY (member_id) REFERENCES members(id)
                        )
                        """);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize database: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement s = conn.createStatement()) {
            // More tolerant settings for restricted environments
            s.execute("PRAGMA journal_mode=MEMORY");
            s.execute("PRAGMA synchronous=OFF");
            s.execute("PRAGMA foreign_keys=ON");
        }
        return conn;
    }

    private DatabaseConnection() {
        // utility class
    }
}
