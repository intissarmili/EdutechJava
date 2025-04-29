package service;

import models.Feed;
import models.FeedStatistics;
import utils.MaConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedService {

    private static final String INSERT_FEED = "INSERT INTO feed (publication, last_modified) VALUES (?, ?)";
    private static final String UPDATE_FEED = "UPDATE feed SET publication = ?, last_modified = ? WHERE id = ?";
    private static final String DELETE_FEED = "DELETE FROM feed WHERE id = ?";
    private static final String GET_ALL_FEEDS = "SELECT * FROM feed ORDER BY last_modified DESC";
    private static final String GET_FEED_BY_ID = "SELECT * FROM feed WHERE id = ?";

    private Connection connection;
    private FeedHistoryService historyService;

    public FeedService() {
        connection = MaConnexion.getInstance().getConnection();
        historyService = new FeedHistoryService();
    }

    public Feed createFeed(Feed feed) throws SQLException {
        // S'assurer que lastModified n'est jamais null
        if (feed.getLastModified() == null) {
            feed.setLastModified(LocalDateTime.now());
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_FEED, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, feed.getPublication());
            pstmt.setTimestamp(2, Timestamp.valueOf(feed.getLastModified()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating feed failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feed.setId(generatedKeys.getInt(1));
                    return feed;
                } else {
                    throw new SQLException("Creating feed failed, no ID obtained.");
                }
            }
        }
    }

    public Feed updateFeed(Feed feed) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_FEED)) {
            
            pstmt.setString(1, feed.getPublication());
            pstmt.setTimestamp(2, Timestamp.valueOf(feed.getLastModified()));
            pstmt.setInt(3, feed.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating feed failed, no rows affected.");
            }
            
            return feed;
        }
    }

    public void deleteFeed(int feedId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(DELETE_FEED)) {
            
            pstmt.setInt(1, feedId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting feed failed, no rows affected.");
            }
        }
    }

    public List<Feed> getAllFeeds() throws SQLException {
        List<Feed> feeds = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_FEEDS)) {
            
            while (rs.next()) {
                feeds.add(mapResultSetToFeed(rs));
            }
        }
        return feeds;
    }

    public Feed getFeedById(int feedId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(GET_FEED_BY_ID)) {
            
            pstmt.setInt(1, feedId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFeed(rs);
                }
            }
        }
        return null;
    }

    private Feed mapResultSetToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getInt("id"));
        feed.setPublication(rs.getString("publication"));
        
        Timestamp lastModified = rs.getTimestamp("last_modified");
        if (lastModified != null) {
            feed.setLastModified(lastModified.toLocalDateTime());
        } else {
            feed.setLastModified(LocalDateTime.now());
        }
        
        return feed;
    }

    public String select() {
        return null;
    }

    public Feed getLastFeed() throws SQLException {
        String query = "SELECT * FROM feed ORDER BY id DESC LIMIT 1";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            LocalDateTime lastModified = null;
            Timestamp timestampLastModified = rs.getTimestamp("last_modified");
            if (timestampLastModified != null) {
                lastModified = timestampLastModified.toLocalDateTime();
            }
            return new Feed(rs.getInt("id"), rs.getString("publication"), lastModified);
        }
        return null;
    }
    
    // Get history for a specific feed
    public List<models.FeedHistory> getFeedHistory(int feedId) throws SQLException {
        return historyService.getHistoryByFeedId(feedId);
    }
    
    // Get all feed history
    public List<models.FeedHistory> getAllFeedHistory() throws SQLException {
        return historyService.getAllHistory();
    }

    // Obtenir les statistiques pour une publication spécifique
    public FeedStatistics getFeedStatistics(int feedId) throws SQLException {
        Feed feed = getFeedById(feedId);
        if (feed == null) {
            throw new SQLException("Feed not found with ID: " + feedId);
        }
        
        String sql = "SELECT " +
                     "COUNT(c.id) as comment_count, " +
                     "SUM(c.up_votes) as total_likes, " +
                     "SUM(c.down_votes) as total_dislikes " +
                     "FROM commentaire c " +
                     "WHERE c.feed_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, feedId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int commentCount = rs.getInt("comment_count");
                    int totalLikes = rs.getInt("total_likes");
                    int totalDislikes = rs.getInt("total_dislikes");
                    
                    // Gérer les cas où SUM retourne NULL (quand il n'y a pas de commentaires)
                    if (rs.wasNull()) {
                        totalLikes = 0;
                        totalDislikes = 0;
                    }
                    
                    return new FeedStatistics(
                        feedId,
                        feed.getPublication(),
                        commentCount,
                        totalLikes,
                        totalDislikes
                    );
                }
            }
        }
        
        // Par défaut, retourne des statistiques vides
        return new FeedStatistics(feedId, feed.getPublication(), 0, 0, 0);
    }
    
    // Obtenir les statistiques pour toutes les publications
    public List<FeedStatistics> getAllFeedStatistics() throws SQLException {
        // Récupérer d'abord toutes les publications
        List<Feed> feeds = getAllFeeds();
        Map<Integer, FeedStatistics> statisticsMap = new HashMap<>();
        
        // Initialiser les statistiques pour chaque publication
        for (Feed feed : feeds) {
            statisticsMap.put(feed.getId(), new FeedStatistics(
                feed.getId(),
                feed.getPublication(),
                0, 0, 0
            ));
        }
        
        // Récupérer les statistiques de commentaires en une seule requête
        String sql = "SELECT " +
                     "c.feed_id, " +
                     "COUNT(c.id) as comment_count, " +
                     "SUM(c.up_votes) as total_likes, " +
                     "SUM(c.down_votes) as total_dislikes " +
                     "FROM commentaire c " +
                     "GROUP BY c.feed_id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int feedId = rs.getInt("feed_id");
                int commentCount = rs.getInt("comment_count");
                int totalLikes = rs.getInt("total_likes");
                int totalDislikes = rs.getInt("total_dislikes");
                
                // Mettre à jour les statistiques si la publication existe
                if (statisticsMap.containsKey(feedId)) {
                    FeedStatistics stats = statisticsMap.get(feedId);
                    stats.setCommentCount(commentCount);
                    stats.setTotalLikes(totalLikes);
                    stats.setTotalDislikes(totalDislikes);
                }
            }
        }
        
        return new ArrayList<>(statisticsMap.values());
    }
}
