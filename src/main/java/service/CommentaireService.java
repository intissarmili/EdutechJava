package service;

import models.Commentaire;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private Connection connection;

    public CommentaireService() {
        this.connection = MaConnexion.getInstance().getConnection();
    }

    // Ajout avec validation et retour de l'ID généré
    public Commentaire addCommentaire(Commentaire c) throws SQLException, IllegalArgumentException {
        // Validation des données
        if (c.getContenu() == null || c.getContenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du commentaire ne peut pas être vide");
        }

        if (c.getContenu().length() > 300) {
            throw new IllegalArgumentException("Le commentaire ne doit pas dépasser 300 caractères");
        }

        if (c.getFeedId() <= 0) {
            throw new IllegalArgumentException("ID de publication invalide");
        }

        String sql = "INSERT INTO commentaire (contenu, feed_id, up_votes, down_votes) VALUES (?, ?, 0, 0)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getContenu());
            stmt.setInt(2, c.getFeedId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de l'ajout du commentaire, aucune ligne affectée");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    c.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de la récupération de l'ID généré");
                }
            }

            return c;
        }
    }

    // Suppression avec vérification d'existence
    public boolean deleteCommentaire(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de commentaire invalide");
        }

        String sql = "DELETE FROM commentaire WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Mise à jour avec validation
    public boolean updateCommentaire(Commentaire c) throws SQLException, IllegalArgumentException {
        if (c.getId() <= 0) {
            throw new IllegalArgumentException("ID de commentaire invalide");
        }

        if (c.getContenu() == null || c.getContenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du commentaire ne peut pas être vide");
        }

        if (c.getContenu().length() > 300) {
            throw new IllegalArgumentException("Le commentaire ne doit pas dépasser 300 caractères");
        }

        String sql = "UPDATE commentaire SET contenu = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getContenu());
            stmt.setInt(2, c.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Like avec vérification d'existence
    public boolean likeCommentaire(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de commentaire invalide");
        }

        String sql = "UPDATE commentaire SET up_votes = up_votes + 1 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Dislike avec vérification d'existence
    public boolean dislikeCommentaire(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de commentaire invalide");
        }

        String sql = "UPDATE commentaire SET down_votes = down_votes + 1 WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Récupération avec gestion des cas vides
    public List<Commentaire> getCommentairesByFeedId(int feedId) throws SQLException {
        if (feedId <= 0) {
            throw new IllegalArgumentException("ID de publication invalide");
        }

        List<Commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE feed_id = ? ORDER BY id DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, feedId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commentaire c = new Commentaire();
                    c.setId(rs.getInt("id"));
                    c.setContenu(rs.getString("contenu"));
                    c.setFeedId(rs.getInt("feed_id"));
                    c.setUpVotes(rs.getInt("up_votes"));
                    c.setDownVotes(rs.getInt("down_votes"));
                    list.add(c);
                }
            }
        }

        return list;
    }

    /**
     * Get the count of comments for a specific feed
     * 
     * @param feedId The ID of the feed
     * @return The number of comments for this feed
     * @throws SQLException If there is an error accessing the database
     * @throws IllegalArgumentException If the feedId is invalid
     */
    public int getCommentCountForFeed(int feedId) throws SQLException {
        if (feedId <= 0) {
            throw new IllegalArgumentException("ID de publication invalide");
        }

        String sql = "SELECT COUNT(*) FROM commentaire WHERE feed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, feedId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }
}