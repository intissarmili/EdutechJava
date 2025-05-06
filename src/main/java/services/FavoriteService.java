package services;

import interfaces.IFavService;
import models.Certification;
import models.Favorite;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService implements IFavService {
    Connection cnx = MaConnexion.getInstance().getConnection();

    public FavoriteService() {
        // Constructeur vide
    }

    @Override
    public void addFavorite(int userId, int certificationId) {
        String sql = "INSERT INTO Favorite (user_id, certification_id) VALUES (?, ?)";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, certificationId);
            st.executeUpdate();
            System.out.println("Certification ajoutée aux favoris.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout aux favoris: " + e.getMessage());
        }
    }

    @Override
    public void removeFavorite(int userId, int certificationId) {
        String sql = "DELETE FROM Favorite WHERE user_id = ? AND certification_id = ?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, certificationId);
            st.executeUpdate();
            System.out.println("Certification supprimée des favoris.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression des favoris: " + e.getMessage());
        }
    }

    @Override
    public List<Favorite> getFavoritesByUserId(int userId) {
        List<Favorite> favorites = new ArrayList<>();
        String sql = "SELECT id, certification_id FROM Favorite WHERE user_id = ?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int certificationId = rs.getInt("certification_id");
                favorites.add(new Favorite(id, userId, certificationId));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des favoris: " + e.getMessage());
        }
        return favorites;
    }

    @Override
    public boolean isFavorite(int userId, int certificationId) {
        String sql = "SELECT COUNT(*) FROM Favorite WHERE user_id = ? AND certification_id = ?";
        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, certificationId);
            ResultSet rs = st.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification des favoris: " + e.getMessage());
        }
        return false;
    }

    // ✅ Nouvelle méthode pour récupérer les objets Certification favoris
    public List<Certification> getCertificationsFavoritesByUserId(int userId) {
        List<Certification> certifications = new ArrayList<>();
        String sql = "SELECT c.* FROM certification c " +
                "JOIN Favorite f ON c.id = f.certification_id " +
                "WHERE f.user_id = ?";

        try (PreparedStatement st = cnx.prepareStatement(sql)) {
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Certification cert = new Certification(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getString("img"),
                        rs.getInt("prix_piece")
                );
                cert.setNote(rs.getObject("note") != null ? rs.getInt("note") : null);
                certifications.add(cert);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des certifications favorites: " + e.getMessage());
        }

        return certifications;
    }
}
