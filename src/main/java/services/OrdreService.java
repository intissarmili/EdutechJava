package services;

import models.Certification;
import models.Ordre;
import utils.MaConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrdreService {
    Connection cnx = MaConnexion.getInstance().getConn();

    // Récupérer les certifications du panier d'un utilisateur
    public List<Certification> getCertificationsInPanier(int userId) {
        List<Certification> certifications = new ArrayList<>();
        String SQL = "SELECT c.* FROM Certification c " +
                "JOIN Ordre o ON c.id = o.certification_id " +
                "WHERE o.user_id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Certification c = new Certification(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("prix"),
                        rs.getString("img")
                );
                certifications.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return certifications;
    }

    // Supprimer une certification du panier
    public void delete(Ordre ordre) {
        String SQL = "DELETE FROM Ordre WHERE user_id = ? AND certification_id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, ordre.getUserId());
            st.setInt(2, ordre.getCertificationId());
            st.executeUpdate();
            System.out.println("Certification supprimée du panier avec succès !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Afficher toutes les certifications dans le panier d’un utilisateur
    public List<Certification> getCertificationsByUser(int userId) {
        List<Certification> certifications = new ArrayList<>();
        String SQL = "SELECT c.* FROM Certification c " +
                "JOIN Ordre o ON c.id = o.certification_id " +
                "WHERE o.user_id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Certification c = new Certification();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                c.setPrix(rs.getInt("prix"));
                c.setImg(rs.getString("img"));
                certifications.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return certifications;
    }

    // Calculer le prix total du panier d’un utilisateur
    public double calculerTotal(int userId) {
        double total = 0;
        String SQL = "SELECT SUM(c.prix) AS total FROM Certification c " +
                "JOIN Ordre o ON c.id = o.certification_id " +
                "WHERE o.user_id = ?";
        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return total;
    }
}
