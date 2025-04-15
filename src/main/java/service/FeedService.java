package service;

import models.Feed;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class FeedService {

     Connection connection;

    public FeedService() {
        this.connection = connection;
        connection = MaConnexion.getInstance().getConnection();
    }



    public void addFeed(Feed feed) throws SQLException {
        String sql = "INSERT INTO feed (publication) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, feed.getPublication());
            stmt.executeUpdate();
        }
    }

    public boolean deleteFeed(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtDeleteComments = null;
        PreparedStatement stmtDeleteFeed = null;
        boolean success = false;

        try {
            // Désactiver l'auto-commit pour faire une transaction
            conn = MaConnexion.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. D'abord, supprimer tous les commentaires associés à cette publication
            String deleteCommentsSQL = "DELETE FROM commentaire WHERE feed_id = ?";
            stmtDeleteComments = conn.prepareStatement(deleteCommentsSQL);
            stmtDeleteComments.setInt(1, id);
            stmtDeleteComments.executeUpdate();

            // 2. Ensuite, supprimer la publication elle-même
            String deleteFeedSQL = "DELETE FROM feed WHERE id = ?";
            stmtDeleteFeed = conn.prepareStatement(deleteFeedSQL);
            stmtDeleteFeed.setInt(1, id);
            int rowsAffected = stmtDeleteFeed.executeUpdate();

            // Valider la transaction si tout s'est bien passé
            conn.commit();

            success = (rowsAffected > 0);
            return success;

        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Relancer l'exception pour qu'elle soit gérée par l'appelant

        } finally {
            // Fermer les ressources et rétablir l'auto-commit
            if (stmtDeleteComments != null) {
                try { stmtDeleteComments.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (stmtDeleteFeed != null) {
                try { stmtDeleteFeed.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void updateFeed(Feed feed) throws SQLException {
        String sql = "UPDATE feed SET publication = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, feed.getPublication());
            stmt.setInt(2, feed.getId());
            stmt.executeUpdate();
        }
    }

    public List<Feed> getAllFeeds() throws SQLException {
        List<Feed> list = new ArrayList<>();
        String sql = "SELECT * FROM feed";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Feed f = new Feed(rs.getInt("id"), rs.getString("publication"));
                list.add(f);
            }
        }
        return list;
    }


    public String select() {
        return null;
    }


    public Feed getLastFeed() throws SQLException {
        String query = "SELECT * FROM feed ORDER BY id DESC LIMIT 1";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            return new Feed(rs.getInt("id"), rs.getString("publication"));
        }
        return null;
    }

}
