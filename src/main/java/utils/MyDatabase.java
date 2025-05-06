package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static MyDatabase instance;
    private final String URL = "jdbc:mysql://127.0.0.1:3306/app";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection cnx;

    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null)
            instance = new MyDatabase();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
