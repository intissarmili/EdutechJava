package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {

    private final String URL = "jdbc:mysql://localhost:3306/app"; // 🔁 base = app
    private final String USER = "root";
    private final String PASSWORD = ""; // 🔁 mets ton mot de passe ici si tu en as un

    private Connection connection;
    private static MaConnexion instance;

    private MaConnexion() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connecté à la base de données MySQL");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    public static MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
