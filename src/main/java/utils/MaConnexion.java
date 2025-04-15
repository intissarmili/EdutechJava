package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
 private final   String url="jdbc:mysql://localhost:3306/app";
private   final   String user ="root";
 private   final String pws ="";

private Connection connection;
private static MaConnexion instance;
private MaConnexion(){
    try {
        connection= DriverManager.getConnection(url,user,pws);
        System.out.println("connecter a la base de données");
    } catch (SQLException e) {
        System.err.println(e.getMessage());    }
}
public static MaConnexion getInstance(){
    if (instance==null){
     instance= new MaConnexion();

    }
    return instance;
}

    public Connection getConnection() {
        return connection;
    }
}
