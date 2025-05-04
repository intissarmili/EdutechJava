package services;

import interfaces.IFavService;
import models.Favorite;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteService implements IFavService {
    Connection cnx = MaConnexion.getInstance().getConn();


    public FavoriteService() {
        // Constructeur sans connexion, peut être utilisé dans les tests
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
                return true; // La certification est déjà un favori
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification des favoris: " + e.getMessage());
        }

        return false; // La certification n'est pas un favori
    }
}
