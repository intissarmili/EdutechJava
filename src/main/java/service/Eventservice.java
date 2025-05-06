package service;

import connect.MyDatabase;
import interfaces.eventinterface;
import models.Event;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Eventservice implements eventinterface<Event> {

    private final Connection connection;
    // Récupérez le nom correct de la colonne date/heure
    private final String dateColumnName = "date";  // Modifiez ceci selon votre base de données: date, time, datetime, created_at, etc.

    public Eventservice() {
        connection = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Event e) {
        String sql = "INSERT INTO event (title, description, categoryevent_id, " + dateColumnName + ", photo) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());

            // Gestion des valeurs NULL pour description
            if (e.getDescription() != null) {
                ps.setString(2, e.getDescription());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }

            ps.setInt(3, e.getCategoryevent_id());
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateTime()));

            // Gestion des valeurs NULL pour photo
            if (e.getPhoto() != null) {
                ps.setString(5, e.getPhoto());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Événement ajouté avec succès !");
            } else {
                System.out.println("⚠️ Aucune ligne n'a été insérée.");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de l'ajout : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(Event e) {
        deleteById(e.getId());
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Événement supprimé avec succès !");
            } else {
                System.out.println("⚠️ Aucun événement trouvé avec l'ID: " + id);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la suppression : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public List<Event> getAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event";

        try {
            // Vérifier la connexion
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ La connexion à la base de données est fermée ou null!");
                return list;
            }

            System.out.println("Exécution de la requête SQL: " + sql);

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Afficher les noms des colonnes pour déboguer
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("Colonnes disponibles dans la table 'event':");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(" - " + metaData.getColumnName(i));
            }

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                int categoryId = rs.getInt("categoryevent_id");
                String photo = rs.getString("photo");

                // Récupérer la date en utilisant le nom correct de la colonne
                LocalDateTime dateTime;
                try {
                    Timestamp timestamp = rs.getTimestamp(dateColumnName);
                    dateTime = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de la récupération de la date: " + e.getMessage());
                    dateTime = LocalDateTime.now();  // Valeur par défaut si erreur
                }

                Event e = new Event(id, title, description, categoryId, dateTime, photo);
                list.add(e);
                count++;
                System.out.println("Événement chargé: ID=" + id + ", Titre=" + title +
                        ", Description=" + description + ", Catégorie=" + categoryId +
                        ", Date=" + dateTime + ", Photo=" + photo);
            }
            System.out.println("📋 Nombre d'événements récupérés: " + count);

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des événements : " + ex.getMessage());
            ex.printStackTrace();
        }

        return list;
    }

    @Override
    public void update(Event e) {
        String sql = "UPDATE event SET title = ?, description = ?, categoryevent_id = ?, " + dateColumnName + " = ?, photo = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());

            // Gestion des valeurs NULL pour description
            if (e.getDescription() != null) {
                ps.setString(2, e.getDescription());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }

            ps.setInt(3, e.getCategoryevent_id());
            ps.setTimestamp(4, Timestamp.valueOf(e.getDateTime()));

            // Gestion des valeurs NULL pour photo
            if (e.getPhoto() != null) {
                ps.setString(5, e.getPhoto());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }

            ps.setInt(6, e.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Événement mis à jour avec succès !");
            } else {
                System.out.println("⚠️ Aucun événement trouvé avec l'ID: " + e.getId());
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la mise à jour : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Méthode utilitaire pour récupérer un événement par son ID
    public Event getById(int id) {
        String sql = "SELECT * FROM event WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime dateTime;
                    try {
                        Timestamp timestamp = rs.getTimestamp(dateColumnName);
                        dateTime = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
                    } catch (SQLException e) {
                        System.err.println("❌ Erreur lors de la récupération de la date: " + e.getMessage());
                        dateTime = LocalDateTime.now();  // Valeur par défaut si erreur
                    }

                    return new Event(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("categoryevent_id"),
                            dateTime,
                            rs.getString("photo")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération de l'événement : " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }
}