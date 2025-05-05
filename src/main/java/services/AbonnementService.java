package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Abonnement;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AbonnementService {

    private final Connection connection;

    public AbonnementService() {
        this.connection = MaConnexion.getInstance().getConnection();
    }

    public void addAbonnement(Abonnement abonnement) {
        String query = "INSERT INTO abonnement (nom, duree, prix, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, abonnement.getNom());
            ps.setString(2, abonnement.getDuree());
            ps.setString(3, abonnement.getPrix());
            ps.setString(4, abonnement.getDescription());
            ps.executeUpdate();
            System.out.println(" Abonnement ajouté avec succès");
        } catch (SQLException e) {
            System.out.println(" Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    public void updateAbonnement(Abonnement ancien, Abonnement nouveau) {
        String query = "UPDATE abonnement SET nom = ?, duree = ?, prix = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nouveau.getNom());
            ps.setString(2, nouveau.getDuree());
            ps.setString(3, nouveau.getPrix());
            ps.setString(4, nouveau.getDescription());
            ps.setInt(5, ancien.getId());
            ps.executeUpdate();
            System.out.println("Abonnement mis à jour");
        } catch (SQLException e) {
            System.out.println(" Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    public void deleteAbonnement(Abonnement abonnement) {
        String query = "DELETE FROM abonnement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, abonnement.getId());
            ps.executeUpdate();
            System.out.println(" Abonnement supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public ObservableList<Abonnement> getAllAbonnements() {
        ObservableList<Abonnement> abonnements = FXCollections.observableArrayList();
        String query = "SELECT * FROM abonnement";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Abonnement a = new Abonnement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("duree"),
                        rs.getString("prix"),
                        rs.getString("description")
                );
                abonnements.add(a);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des abonnements : " + e.getMessage());
        }
        return abonnements;
    }
}
