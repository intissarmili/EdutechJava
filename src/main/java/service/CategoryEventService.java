package service;

import connect.MyDatabase;
import interfaces.categoryeventinterface;
import model.CategoryEvent;
import model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryEventService implements categoryeventinterface {

    private final Connection connection;

    public CategoryEventService() {
        connection = MyDatabase.getInstance().getCnx();
    }

    @Override
    // Méthode pour ajouter une catégorie d'événement
    public boolean add(CategoryEvent category) {  // Changé pour retourner boolean
        String query = "INSERT INTO categoryevent (location, type, duration) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getLocation());
            ps.setString(2, category.getType());
            ps.setString(3, category.getDuration());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
                return true; // Retourne true si l'ajout est réussi
            }
            return false; // Retourne false si aucune ligne n'a été affectée
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la catégorie: " + e.getMessage());
            e.printStackTrace();
            return false; // Retourne false en cas d'erreur
        }
    }

    @Override
    public void delete(CategoryEvent categoryEvent) {
        deleteById(categoryEvent.getId());
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM categoryevent WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Catégorie d'événement supprimée avec succès !");
            } else {
                System.out.println("⚠️ Aucune catégorie trouvée avec l'ID: " + id);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la suppression : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public List<CategoryEvent> getAll() {
        List<CategoryEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM categoryevent";

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
            System.out.println("Colonnes disponibles dans la table 'categoryevent':");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(" - " + metaData.getColumnName(i));
            }

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String location = rs.getString("location");
                String type = rs.getString("type");
                String duration = rs.getString("duration");

                CategoryEvent category = new CategoryEvent(id, location, type, duration);
                list.add(category);
                count++;
                System.out.println("Catégorie chargée: ID=" + id + ", Lieu=" + location +
                        ", Type=" + type + ", Durée=" + duration);
            }
            System.out.println("📋 Nombre de catégories récupérées: " + count);

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des catégories : " + ex.getMessage());
            ex.printStackTrace();
        }

        return list;
    }

    @Override
    public void update(CategoryEvent categoryEvent) {
        String sql = "UPDATE categoryevent SET location = ?, type = ?, duration = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryEvent.getLocation());
            ps.setString(2, categoryEvent.getType());
            ps.setString(3, categoryEvent.getDuration());
            ps.setInt(4, categoryEvent.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Catégorie d'événement mise à jour avec succès !");
            } else {
                System.out.println("⚠️ Aucune catégorie trouvée avec l'ID: " + categoryEvent.getId());
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la mise à jour : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public List<CategoryEvent> getAllCategories() {
        List<CategoryEvent> categories = new ArrayList<>();
        String query = "SELECT * FROM categoryevent";
        try {
            Connection conn = MyDatabase.getInstance().getCnx();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String location = rs.getString("location");
                String duration = rs.getString("duration");  // Changé de description à duration

                CategoryEvent category = new CategoryEvent(id, location, type, duration);
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des catégories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    // Méthode utilitaire pour récupérer une catégorie par son ID
    public CategoryEvent getById(int id) {
        String sql = "SELECT * FROM categoryevent WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CategoryEvent(
                            rs.getInt("id"),
                            rs.getString("location"),
                            rs.getString("type"),
                            rs.getString("duration")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération de la catégorie : " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }
}