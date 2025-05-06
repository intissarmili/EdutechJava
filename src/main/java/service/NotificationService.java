package service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.Notification;
import models.Notification.NotificationType;
import utils.MaConnexion;

/**
 * Service class for handling notification operations
 */
public class NotificationService {
    
    private Connection connection;
    
    /**
     * Constructor that initializes the database connection
     */
    public NotificationService() {
        connection = MaConnexion.getInstance().getConnection();
    }
    
    /**
     * Create a new notification in the database
     * 
     * @param notification The notification to create
     * @return The created notification with its generated ID
     */
    public Notification createNotification(Notification notification) {
        String query = "INSERT INTO notification (user_id, content, type, creation_date, is_read, related_entity_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getContent());
            ps.setString(3, notification.getType().toString());
            ps.setTimestamp(4, new Timestamp(notification.getCreationDate().getTime()));
            ps.setBoolean(5, notification.isRead());
            
            if (notification.getRelatedEntityId() != null) {
                ps.setInt(6, notification.getRelatedEntityId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                }
            }
            
            return notification;
        } catch (SQLException e) {
            System.out.println("Error creating notification: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all notifications for a specific user
     * 
     * @param userId User ID to get notifications for
     * @return List of notifications
     */
    public List<Notification> getNotificationsByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE user_id = ? ORDER BY creation_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
            
            return notifications;
        } catch (SQLException e) {
            System.out.println("Error retrieving notifications: " + e.getMessage());
            return notifications;
        }
    }
    
    /**
     * Get unread notifications for a specific user
     * 
     * @param userId User ID to get notifications for
     * @return List of unread notifications
     */
    public List<Notification> getUnreadNotificationsByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE user_id = ? AND is_read = false ORDER BY creation_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
            
            return notifications;
        } catch (SQLException e) {
            System.out.println("Error retrieving unread notifications: " + e.getMessage());
            return notifications;
        }
    }
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId Notification ID to mark as read
     * @return True if successful, false otherwise
     */
    public boolean markAsRead(int notificationId) {
        String query = "UPDATE notification SET is_read = true WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark all notifications for a user as read
     * 
     * @param userId User ID to mark all notifications as read
     * @return Number of notifications marked as read
     */
    public int markAllAsRead(int userId) {
        String query = "UPDATE notification SET is_read = true WHERE user_id = ? AND is_read = false";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error marking all notifications as read: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Delete a notification by ID
     * 
     * @param notificationId Notification ID to delete
     * @return True if successful, false otherwise
     */
    public boolean deleteNotification(int notificationId) {
        String query = "DELETE FROM notification WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete notifications for a specific entity
     * 
     * @param relatedEntityId Entity ID to delete notifications for
     * @return Number of notifications deleted
     */
    public int deleteNotificationsByEntityId(int relatedEntityId) {
        String query = "DELETE FROM notification WHERE related_entity_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, relatedEntityId);
            
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting notifications for entity: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Count unread notifications for a user
     * 
     * @param userId User ID to count notifications for
     * @return Number of unread notifications
     */
    public int getUnreadNotificationCount(int userId) {
        String query = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = false";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return 0;
        } catch (SQLException e) {
            System.out.println("Error counting unread notifications: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Create notification for a new post
     * 
     * @param userId User to notify
     * @param authorName Name of the post author
     * @param feedId Feed ID
     * @return Created notification
     */
    public Notification createPostNotification(int userId, String authorName, int feedId) {
        String content = authorName + " has created a new post";
        Notification notification = new Notification(
            userId, 
            content,
            NotificationType.POST, 
            feedId
        );
        return createNotification(notification);
    }
    
    /**
     * Create notification for a new comment
     * 
     * @param userId User to notify
     * @param commenterName Name of the commenter
     * @param feedId Feed ID that was commented on
     * @return Created notification
     */
    public Notification createCommentNotification(int userId, String commenterName, int feedId) {
        String content = commenterName + " commented on your post";
        Notification notification = new Notification(
            userId, 
            content,
            NotificationType.COMMENT, 
            feedId
        );
        return createNotification(notification);
    }
    
    /**
     * Create notification for a like
     * 
     * @param userId User to notify
     * @param likerName Name of the person who liked
     * @param feedId Feed ID that was liked
     * @return Created notification
     */
    public Notification createLikeNotification(int userId, String likerName, int feedId) {
        String content = likerName + " liked your post";
        Notification notification = new Notification(
            userId, 
            content,
            NotificationType.LIKE, 
            feedId
        );
        return createNotification(notification);
    }
    
    /**
     * Get all notifications from the database
     * 
     * @return List of all notifications
     */
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notification ORDER BY creation_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
            
            return notifications;
        } catch (SQLException e) {
            System.out.println("Error retrieving all notifications: " + e.getMessage());
            return notifications;
        }
    }
    
    /**
     * Map a ResultSet to a Notification object
     * 
     * @param rs ResultSet from database query
     * @return Mapped Notification object
     * @throws SQLException If error occurs during mapping
     */
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setContent(rs.getString("content"));
        notification.setType(NotificationType.valueOf(rs.getString("type")));
        notification.setCreationDate(rs.getTimestamp("creation_date"));
        notification.setRead(rs.getBoolean("is_read"));
        
        // Handle potential NULL value for related_entity_id
        int relatedEntityId = rs.getInt("related_entity_id");
        if (!rs.wasNull()) {
            notification.setRelatedEntityId(relatedEntityId);
        }
        
        return notification;
    }
    
    /**
     * Add a new notification to the database
     * 
     * @param notification The notification to add
     * @return The added notification with its generated ID
     */
    public Notification addNotification(Notification notification) {
        String query = "INSERT INTO notification (user_id, content, type, creation_date, is_read, related_entity_id) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getContent());
            ps.setString(3, notification.getType().toString());
            ps.setTimestamp(4, new Timestamp(notification.getCreationDate().getTime()));
            ps.setBoolean(5, notification.isRead());
            
            if (notification.getRelatedEntityId() != null) {
                ps.setInt(6, notification.getRelatedEntityId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                }
            }
            
            return notification;
        } catch (SQLException e) {
            System.out.println("Error adding notification: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update an existing notification in the database
     * 
     * @param notification The notification to update
     * @return True if the update was successful, false otherwise
     */
    public boolean updateNotification(Notification notification) {
        String query = "UPDATE notification SET user_id = ?, content = ?, type = ?, creation_date = ?, " +
                      "is_read = ?, related_entity_id = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getContent());
            ps.setString(3, notification.getType().toString());
            ps.setTimestamp(4, new Timestamp(notification.getCreationDate().getTime()));
            ps.setBoolean(5, notification.isRead());
            
            if (notification.getRelatedEntityId() != null) {
                ps.setInt(6, notification.getRelatedEntityId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            
            ps.setInt(7, notification.getId());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
            return false;
        }
    }
}
