package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    // Make sure 'PoliceDB' is the exact name of your database in MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/PoliceDB";
    // Update these two lines with your new account details
    private static final String USER = "newuser";
    private static final String PASS = "Newuser123";

    public static Connection getConnection() {
        try {
            // This is the line that actually opens the door to the database
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("Database Connection Failed! Check your password.");
            return null;
        }
    }
}