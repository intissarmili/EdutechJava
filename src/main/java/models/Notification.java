package models;

import java.util.Date;

/**
 * Model class representing a notification
 */
public class Notification {
    
    /**
     * Enumeration of notification types
     */
    public enum NotificationType {
        POST,       // New post notification
        COMMENT,    // New comment notification
        LIKE,       // New like notification
        DISLIKE,    // New dislike notification
        MENTION,    // User mention notification
        SYSTEM      // System notification
    }
    
    private int id;
    private int userId;
    private String content;
    private NotificationType type;
    private Date creationDate;
    private boolean read;
    private Integer relatedEntityId; // ID of related feed, comment, etc.
    
    /**
     * Default constructor
     */
    public Notification() {
        this.creationDate = new Date();
        this.read = false;
    }
    
    /**
     * Constructor with all parameters
     * 
     * @param id Notification ID
     * @param userId User ID the notification is for
     * @param content Notification content
     * @param type Notification type
     * @param relatedEntityId Related entity ID (optional)
     */
    public Notification(int id, int userId, String content, NotificationType type, Integer relatedEntityId) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.creationDate = new Date();
        this.read = false;
        this.relatedEntityId = relatedEntityId;
    }
    
    /**
     * Constructor for creating a new notification (without ID)
     * 
     * @param userId User ID the notification is for
     * @param content Notification content
     * @param type Notification type
     * @param relatedEntityId Related entity ID (optional)
     */
    public Notification(int userId, String content, NotificationType type, Integer relatedEntityId) {
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.creationDate = new Date();
        this.read = false;
        this.relatedEntityId = relatedEntityId;
    }

    // Getters and setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", creationDate=" + creationDate +
                ", read=" + read +
                ", relatedEntityId=" + relatedEntityId +
                '}';
    }
}
