package service;

import service.IServicee;
import models.Certification;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationService implements IServicee <Certification> {
    private Connection cnx;

    public CertificationService() {
        cnx = MaConnexion.getInstance().getConnection();
    }

    @Override
    public void create(Certification certification) throws SQLException {
        String query = "INSERT INTO certification(nom, description, prix, img, prix_piece) " +
                "VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, certification.getNom());
            ps.setString(2, certification.getDescription());
            ps.setDouble(3, certification.getPrix());
            ps.setString(4, certification.getImg());
            ps.setInt(5, certification.getPrixPiece());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    certification.setId(rs.getInt(1)); // Set the ID generated
                }
            }
        }
    }

    @Override
    public void update(Certification certification) throws SQLException {
        String query = "UPDATE certification SET nom = ?, description = ?, prix = ?, " +
                "img = ?, prix_piece = ?, note = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, certification.getNom());
            ps.setString(2, certification.getDescription());
            ps.setDouble(3, certification.getPrix());
            ps.setString(4, certification.getImg());
            ps.setInt(5, certification.getPrixPiece());
            ps.setObject(6, certification.getNote(), Types.INTEGER);
            ps.setInt(7, certification.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Certification certification) throws SQLException {
        String query = "DELETE FROM certification WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, certification.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Certification> readAll() throws SQLException {
        List<Certification> certifications = new ArrayList<>();
        String query = "SELECT * FROM certification";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

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
        }
        return certifications;
    }


    public Certification findById(int id) throws SQLException {
        String query = "SELECT * FROM certification WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Certification cert = new Certification(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getDouble("prix"),
                            rs.getString("img"),
                            rs.getInt("prix_piece")

                    );
                    cert.setNote(rs.getObject("note") != null ? rs.getInt("note") : null);
                    return cert;
                }
            }
        }
        return null;
    }

    public boolean certificationExists(int id) throws SQLException {
        String query = "SELECT COUNT(*) FROM certification WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<Integer> getAvailableCertificationIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM certification";

        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }

        return ids;
    }

    public List<Integer> getAllCertificationIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM certification";
        try (PreparedStatement statement = cnx.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ids.add(resultSet.getInt("id"));
            }
        }
        return ids;
    }
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
    private Certification mapResultSetToCertification(ResultSet rs) throws SQLException {
        return new Certification(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getInt("prix"),
                rs.getString("img"),
                rs.getInt("prix_piece")

        );
    }
}
