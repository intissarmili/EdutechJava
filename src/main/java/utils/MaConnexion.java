package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    final String URL="jdbc:mysql://localhost:3306/app";
    final String username="root";
    final String password="";

    private static MaConnexion instance;
    private Connection connection;


    public static   MaConnexion getInstance()
    {
        if (instance == null) {
                instance = new MaConnexion();
        }

        return instance;
    }

    public Connection getConnection()
    {

        return connection;
    }




    private MaConnexion()
    {
        try {

            connection= DriverManager.getConnection(URL,username,password);
            System.out.println("connection etablie avec succes");



        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }



    }












}

