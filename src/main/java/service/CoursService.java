package service;

import interfaces.IService;
import models.Cours;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {
    private final Connection cnx;

    public CoursService() {
        cnx = MaConnexion.getInstance().getConnection();
    }

    public List<Integer> getAllCoursIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM cours";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }
        return ids;
    }

    public boolean isCertificationUsed(int certificationId) throws SQLException {
        String query = "SELECT COUNT(*) FROM cours WHERE certification_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, certificationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void create(Cours cours) throws SQLException {
        if (isCertificationUsed(cours.getCertificationId())) {
            throw new SQLException("La certification ID " + cours.getCertificationId() + " est déjà utilisée");
        }

        String query = "INSERT INTO cours(titre, contenu, categorie, certification_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getContenu());
            ps.setString(3, cours.getCategorie());
            ps.setInt(4, cours.getCertificationId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cours.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Cours cours) throws SQLException {
        String query = "UPDATE cours SET titre = ?, contenu = ?, categorie = ?, certification_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, cours.getTitre());
            ps.setString(2, cours.getContenu());
            ps.setString(3, cours.getCategorie());
            ps.setInt(4, cours.getCertificationId());
            ps.setInt(5, cours.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Cours cours) throws SQLException {
        String query = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, cours.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Cours> readAll() throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String query = "SELECT * FROM cours";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Cours c = new Cours(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie"),
                        rs.getInt("certification_id")
                );
                coursList.add(c);
            }
        }
        return coursList;
    }
}