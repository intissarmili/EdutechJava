package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {

    private final String URL = "jdbc:mysql://localhost:3306/app";
    private final String USER = "root";
    private final String PASSWORD = "";

    private Connection connection;
    private static MaConnexion instance;

    private MaConnexion() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connecté à la base de données MySQL (connexion initiale)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion initiale : " + e.getMessage());
        }
    }

    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("🔄 Connexion MySQL réinitialisée");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de connexion : " + e.getMessage());
        }
        return connection;
    }
}
