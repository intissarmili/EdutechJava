package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    private final String URL="jdbc:mysql://localhost:3306/app";
    private final String USERNAME="root";
    private final String PASSWORD="";


    static MaConnexion instance;


    Connection connection;


    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public Connection getConn() {
        return connection;
    }



    private MaConnexion() {
        try {
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("Connection established");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
