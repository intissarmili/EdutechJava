package services;

import interfaces.ICertifService;
import models.Certification;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationService implements ICertifService {
    Connection cnx = MaConnexion.getInstance().getConn();

    @Override
    public List<Certification> getAll() {
        List<Certification> certifications = new ArrayList<>();
        String SQL = "SELECT * FROM Certification";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(SQL);

            while (rs.next()) {
                Certification certif = mapResultSetToCertification(rs);
                certifications.add(certif);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des certifications : " + e.getMessage());
        }

        return certifications;
    }

    @Override
    public Certification getById(int id) {
        String SQL = "SELECT * FROM Certification WHERE id = ?";
        Certification certif = null;

        try {
            PreparedStatement st = cnx.prepareStatement(SQL);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                certif = mapResultSetToCertification(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la certification par ID : " + e.getMessage());
        }

        return certif;
    }

    // Méthode privée pour éviter la répétition (mapping ResultSet -> Certification)
    private Certification mapResultSetToCertification(ResultSet rs) throws SQLException {
        return new Certification(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getInt("prix"),
                rs.getString("img")
        );
    }
}
