package service;

import models.Feed;
import models.FeedHistory;
import utils.MaConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedHistoryService {
    private Connection connection;

    public FeedHistoryService() {
        connection = MaConnexion.getInstance().getConnection();
    }

    // Add a history record
    public void addHistory(FeedHistory history) throws SQLException {
        String sql = "INSERT INTO feed_history (feed_id, old_content, new_content, action_type, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, history.getFeedId());
            stmt.setString(2, history.getOldContent());
            stmt.setString(3, history.getNewContent());
            stmt.setString(4, history.getActionType());
            stmt.setTimestamp(5, Timestamp.valueOf(history.getTimestamp()));
            stmt.executeUpdate();
        }
    }

    // Track a new feed creation
    public void trackCreation(Feed feed) throws SQLException {
        FeedHistory history = new FeedHistory(
                feed.getId(),
                null, // No old content for a new feed
                feed.getPublication(),
                "CREATE"
        );
        addHistory(history);
    }

    // Track a feed update
    public void trackUpdate(Feed oldFeed, Feed newFeed) throws SQLException {
        FeedHistory history = new FeedHistory(
                newFeed.getId(),
                oldFeed.getPublication(),
                newFeed.getPublication(),
                "UPDATE"
        );
        addHistory(history);
    }

    // Track a feed deletion
    public void trackDeletion(Feed feed) throws SQLException {
        FeedHistory history = new FeedHistory(
                feed.getId(),
                feed.getPublication(),
                null, // No new content for a deleted feed
                "DELETE"
        );
        addHistory(history);
    }

    // Get all history for a specific feed
    public List<FeedHistory> getHistoryByFeedId(int feedId) throws SQLException {
        List<FeedHistory> historyList = new ArrayList<>();
        String sql = "SELECT * FROM feed_history WHERE feed_id = ? ORDER BY timestamp DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, feedId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FeedHistory history = new FeedHistory(
                            rs.getInt("id"),
                            rs.getInt("feed_id"),
                            rs.getString("old_content"),
                            rs.getString("new_content"),
                            rs.getString("action_type"),
                            rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    historyList.add(history);
                }
            }
        }
        return historyList;
    }

    // Get all history entries
    public List<FeedHistory> getAllHistory() throws SQLException {
        List<FeedHistory> historyList = new ArrayList<>();
        String sql = "SELECT * FROM feed_history ORDER BY timestamp DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FeedHistory history = new FeedHistory(
                        rs.getInt("id"),
                        rs.getInt("feed_id"),
                        rs.getString("old_content"),
                        rs.getString("new_content"),
                        rs.getString("action_type"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                );
                historyList.add(history);
            }
        }
        return historyList;
    }
} 